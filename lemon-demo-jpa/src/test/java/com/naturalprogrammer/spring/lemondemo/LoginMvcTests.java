package com.naturalprogrammer.spring.lemondemo;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Sql({"/test-data/initialize.sql", "/test-data/finalize.sql"})
public class LoginMvcTests extends AbstractMvcTests {
	
	@Test
	public void testLogin() throws Exception {
		
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
	public void testLoginTokenExpiry() throws Exception {
		
//		// Test that default token does not expire before 10 days		
//		Thread.sleep(1001L);
//		mvc.perform(get("/api/core/ping")
//				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, tokens.get(ADMIN_ID)))
//				.andExpect(status().is(204));
		
		// Test that a 500ms token does not expire before 500ms
		String token = login(ADMIN_EMAIL, ADMIN_PASSWORD, 500L);
//		mvc.perform(get("/api/core/ping")
//				.header(LemonSecurityConfig.TOKEN_REQUEST_HEADER_NAME, token))
//				.andExpect(status().is(204));
		// but, does expire after 500ms
		Thread.sleep(501L);
		mvc.perform(get("/api/core/ping")
				.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().is(401));
	}

	/**
	 * Token won't work if the credentials of the user gets updated afterwards
	 */
	@Test
	public void testObsoleteToken() throws Exception {
		
		// credentials updated
		// Thread.sleep(1001L);		
		User user = userRepository.findById(ADMIN_ID).get();
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		userRepository.save(user);
		
		mvc.perform(get("/api/core/ping")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
				.andExpect(status().is(401));
	}

	@Test
	public void testLoginWrongPassword() throws Exception {
		
		mvc.perform(post("/api/core/login")
                .param("username", ADMIN_EMAIL)
                .param("password", "wrong-password")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}

	@Test
	public void testLoginBlankPassword() throws Exception {
		
		mvc.perform(post("/api/core/login")
                .param("username", ADMIN_EMAIL)
                .param("password", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(401));
	}

	@Test
	public void testTokenLogin() throws Exception {
		
		mvc.perform(get("/api/core/context")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.user.id").value(ADMIN_ID));
	}

	@Test
	public void testTokenLoginWrongToken() throws Exception {
		
		mvc.perform(get("/api/core/context")
				.header(HttpHeaders.AUTHORIZATION, "Bearer a-wrong-token"))
				.andExpect(status().is(401));
	}
	
	@Test
	public void testLogout() throws Exception {
		
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
