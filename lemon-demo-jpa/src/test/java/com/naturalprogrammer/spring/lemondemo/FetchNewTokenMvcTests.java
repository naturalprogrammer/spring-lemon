package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FetchNewTokenMvcTests extends AbstractMvcTests {
	
	public static class Response {
		
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}		
	}

	@Test
	public void testFetchNewToken() throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/fetch-new-auth-token")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andExpect(jsonPath("$.token").value(containsString(".")))
				.andReturn();

		Response response = LecUtils.fromJson(result.getResponse().getContentAsString(), Response.class);
		ensureTokenWorks(response.getToken());
	}
	
	@Test
	public void testFetchNewTokenExpiration() throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/fetch-new-auth-token")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
		        .param("expirationMillis", "1000")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andReturn();

		Response response = LecUtils.fromJson(result.getResponse().getContentAsString(), Response.class);
		ensureTokenWorks(response.getToken());

		Thread.sleep(1001L);
		mvc.perform(get("/api/core/context")
				.header(HttpHeaders.AUTHORIZATION,
						LecUtils.TOKEN_PREFIX + response.getToken()))
				.andExpect(status().is(401));
		
	}

	@Test
	public void testFetchNewTokenByAdminForAnotherUser() throws Exception {
		
		MvcResult result = mvc.perform(post("/api/core/fetch-new-auth-token")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID))
		        .param("username", UNVERIFIED_USER_EMAIL)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(200))
				.andReturn();

		Response response = LecUtils.fromJson(result.getResponse().getContentAsString(), Response.class);
		ensureTokenWorks(response.getToken());
	}
	
	@Test
	public void testFetchNewTokenByNonAdminForAnotherUser() throws Exception {
		
		mvc.perform(post("/api/core/fetch-new-auth-token")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID))
		        .param("username", ADMIN_EMAIL)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(403));
	}
}
