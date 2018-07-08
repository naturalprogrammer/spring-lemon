package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.naturalprogrammer.spring.lemon.security.JwtService;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

public class ResetPasswordMvcTests extends AbstractMvcTests {
	
	private String forgotPasswordCode;
	
	@Autowired
	private JwtService jwtService;
	
	@Before
	public void setUp() {
		
		forgotPasswordCode = jwtService.createToken(
				JwtService.FORGOT_PASSWORD_AUDIENCE,
				ADMIN_EMAIL, 60000L);
	}

	@Test
	public void testResetPassword() throws Exception {
		
		final String NEW_PASSWORD = "newPassword!";
		
		//Thread.sleep(1001L);
		
		mvc.perform(post("/api/core/reset-password")
                .param("code", forgotPasswordCode)
                .param("newPassword", NEW_PASSWORD)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
		        .andExpect(status().is(200))
				.andExpect(header().string(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").value(ADMIN_ID));
		
		// New password should work
		login(ADMIN_EMAIL, NEW_PASSWORD);
		
	    // Repeating shouldn't work
		mvc.perform(post("/api/core/reset-password")
                .param("code", forgotPasswordCode)
                .param("newPassword", NEW_PASSWORD)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
		        .andExpect(status().is(401));
	}
	
	@Test
	public void testResetPasswordInvalidData() throws Exception {
		
		// Wrong code
		mvc.perform(post("/api/core/reset-password")
                .param("code", "wrong-code")
                .param("newPassword", "abc99!")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
		        .andExpect(status().is(401));

		// Blank password
		mvc.perform(post("/api/core/reset-password")
                .param("code", forgotPasswordCode)
                .param("newPassword", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
		        .andExpect(status().is(422));

		// Invalid password
		mvc.perform(post("/api/core/reset-password")
                .param("code", forgotPasswordCode)
                .param("newPassword", "abc")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
		        .andExpect(status().is(422));
	}
}
