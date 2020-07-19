package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VerificationMvcTests extends AbstractMvcTests {
	
	private String verificationCode;
	
	@Autowired
	private GreenTokenService greenTokenService;
	
	@BeforeEach
	public void setUp() {
		
		verificationCode = greenTokenService.createToken(GreenTokenService.VERIFY_AUDIENCE,
				Long.toString(UNVERIFIED_USER_ID), 60000L,
				LecUtils.mapOf("email", UNVERIFIED_USER_EMAIL));
	}
	
	@Test
	void testEmailVerification() throws Exception {
		
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").value(UNVERIFIED_USER_ID))
				.andExpect(jsonPath("$.roles").value(hasSize(0)))
				.andExpect(jsonPath("$.unverified").value(false))
				.andExpect(jsonPath("$.goodUser").value(true));
		
		// Already verified
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));
	}
	
	@Test
	void testEmailVerificationNonExistingUser() throws Exception {
		
		mvc.perform(post("/api/core/users/99/verification")
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(404));
	}
	
	@Test
	void testEmailVerificationWrongToken() throws Exception {
		
		// null token
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(400));

		// blank token
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));

		// Wrong audience
		String token = greenTokenService.createToken("wrong-audience",
				Long.toString(UNVERIFIED_USER_ID), 60000L,
				LecUtils.mapOf("email", UNVERIFIED_USER_EMAIL));
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", token)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
		
		// Wrong email
		token = greenTokenService.createToken(GreenTokenService.VERIFY_AUDIENCE,
				Long.toString(UNVERIFIED_USER_ID), 60000L,
				LecUtils.mapOf("email", "wrong.email@example.com"));
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", token)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(403));

		// expired token
		token = greenTokenService.createToken(GreenTokenService.VERIFY_AUDIENCE,
				Long.toString(UNVERIFIED_USER_ID), 1L,
				LecUtils.mapOf("email", UNVERIFIED_USER_EMAIL));	
		// Thread.sleep(1001L);
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", token)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}
	
	@Test
	void testEmailVerificationAfterCredentialsUpdate() throws Exception {
		
		// Credentials updated after the verification token is issued
		User user = userRepository.findById(UNVERIFIED_USER_ID).orElseThrow(LexUtils.notFoundSupplier());
		user.setCredentialsUpdatedMillis(System.currentTimeMillis() + 1);
		userRepository.save(user);
		
		mvc.perform(post("/api/core/users/{userId}/verification", UNVERIFIED_USER_ID)
                .param("code", verificationCode)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}
}
