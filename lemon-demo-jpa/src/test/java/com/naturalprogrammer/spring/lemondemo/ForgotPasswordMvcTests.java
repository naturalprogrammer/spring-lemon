package com.naturalprogrammer.spring.lemondemo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

public class ForgotPasswordMvcTests extends AbstractMvcTests {
	
	@Test
	public void testForgotPassword() throws Exception {
		
		mvc.perform(post("/api/core/forgot-password")
                .param("email", ADMIN_EMAIL)
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(204));
		
		verify(mailSender).send(any());
	}
	
	@Test
	public void testForgotPasswordInvalidEmail() throws Exception {
		
		// Unknown email
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "unknown@example.com")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(404));

		// Null email
		mvc.perform(post("/api/core/forgot-password")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(400));

		// Blank email
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));
		
		// Wrong email format
		mvc.perform(post("/api/core/forgot-password")
                .param("email", "wrong-email-format")
                .header("contentType",  MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(422));
		
		verify(mailSender, never()).send(any());
	}
}
