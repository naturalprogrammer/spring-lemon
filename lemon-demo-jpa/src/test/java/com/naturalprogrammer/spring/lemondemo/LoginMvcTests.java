package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
class LoginMvcTests extends AbstractMvcTests {
	
	@Test
	void testLogin() throws Exception {
		
		mvc.perform(post("/api/core/login")
                .param("username", ADMIN_EMAIL)
                .param("password", ADMIN_PASSWORD)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andExpect(header().string(LecUtils.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
				.andExpect(jsonPath("$.id").value(ADMIN_ID))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.username").value("admin@example.com"))
				.andExpect(jsonPath("$.roles").value(hasSize(1)))
				.andExpect(jsonPath("$.roles[0]").value("ADMIN"))
				.andExpect(jsonPath("$.tag.name").value("Admin 1"))
				.andExpect(jsonPath("$.unverified").value(false))
				.andExpect(jsonPath("$.blocked").value(false))
				.andExpect(jsonPath("$.admin").value(true))
				.andExpect(jsonPath("$.goodUser").value(true))
				.andExpect(jsonPath("$.goodAdmin").value(true));
	}

	@Test
	void testLoginTokenExpiry() throws Exception {
		
		// Test that default token works (does not expire before 10 days)
		mvc.perform(get("/api/core/ping")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
				.andExpect(status().is(204));
		
		// Test that a 5000ms token does not expire before 5000ms
		String token = login(ADMIN_EMAIL, ADMIN_PASSWORD, 5000L);
		mvc.perform(get("/api/core/ping")
				.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().is(204));

		// Test that a 500ms token expires after 500ms
		token = login(ADMIN_EMAIL, ADMIN_PASSWORD, 500L);
		Thread.sleep(501L);
		mvc.perform(get("/api/core/ping")
				.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().is(401));
	}

	/**
	 * Token won't work if the credentials of the user gets updated afterwards
	 */
	@Test
	void testObsoleteToken() throws Exception {
		
		// credentials updated
		User user = userRepository.findById(ADMIN_ID).get();
		user.setCredentialsUpdatedMillis(System.currentTimeMillis() + 1);
		userRepository.save(user);
		
		mvc.perform(get("/api/core/ping")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
				.andExpect(status().is(401));
	}

	@Test
	void testLoginWrongPassword() throws Exception {
		
		mvc.perform(post("/api/core/login")
                .param("username", ADMIN_EMAIL)
                .param("password", "wrong-password")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}

	@Test
	void testLoginBlankPassword() throws Exception {
		
		mvc.perform(post("/api/core/login")
                .param("username", ADMIN_EMAIL)
                .param("password", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}

	@Test
	void testTokenLogin() throws Exception {
		
		mvc.perform(get("/api/core/context")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.user.id").value(ADMIN_ID));
	}

	@Test
	void testTokenLoginWrongToken() throws Exception {
		
		mvc.perform(get("/api/core/context")
				.header(HttpHeaders.AUTHORIZATION, "Bearer a-wrong-token"))
				.andExpect(status().is(401));
	}
	
	@Test
	void testLogout() throws Exception {
		
		mvc.perform(post("/logout"))
                .andExpect(status().is(404));
	}
	
	private String login(String username, String password, long expirationMillis) throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/login")
                .param("username", ADMIN_EMAIL)
                .param("password", ADMIN_PASSWORD)
                .param("expirationMillis", Long.toString(expirationMillis))
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();

		return result.getResponse().getHeader(LecUtils.TOKEN_RESPONSE_HEADER_NAME);     
	}
}
