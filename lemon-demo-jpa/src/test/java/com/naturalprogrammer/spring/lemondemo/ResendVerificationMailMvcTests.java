package com.naturalprogrammer.spring.lemondemo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class ResendVerificationMailMvcTests extends AbstractMvcTests {

	@Test
	public void testResendVerificationMail() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_USER_ID)))
			.andExpect(status().is(204));
		
		verify(mailSender).send(any());
	}

	@Test
	public void testAdminResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
			.andExpect(status().is(204));
	}

	@Test
	public void testBadAdminResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(UNVERIFIED_ADMIN_ID)))
			.andExpect(status().is(403));
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(BLOCKED_ADMIN_ID)))
			.andExpect(status().is(403));
		
		verify(mailSender, never()).send(any());
	}

	@Test
	public void testResendVerificationMailUnauthenticated() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID))
			.andExpect(status().is(403));
		
		verify(mailSender, never()).send(any());
	}
	
	@Test
	public void testResendVerificationMailAlreadyVerified() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(USER_ID)))
			.andExpect(status().is(422));
		
		verify(mailSender, never()).send(any());
	}
	
	@Test
	public void testResendVerificationMailOtherUser() throws Exception {
		
		mvc.perform(post("/api/core/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
				.header(HttpHeaders.AUTHORIZATION, tokens.get(USER_ID)))
			.andExpect(status().is(403));
		
		verify(mailSender, never()).send(any());
	}
	
	@Test
	public void testResendVerificationMailNonExistingUser() throws Exception {
		
		mvc.perform(post("/api/core/users/99/resend-verification-mail")
				.header(HttpHeaders.AUTHORIZATION, tokens.get(ADMIN_ID)))
			.andExpect(status().is(404));
		
		verify(mailSender, never()).send(any());
	}
}
