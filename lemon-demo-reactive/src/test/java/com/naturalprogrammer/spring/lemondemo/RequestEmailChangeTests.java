package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.CLIENT;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.TOKENS;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.UNVERIFIED_ADMIN_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.UNVERIFIED_USER_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.UNVERIFIED_USER_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.USER_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.USER_PASSWORD;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemondemo.dto.TestEmailForm;
import com.naturalprogrammer.spring.lemondemo.dto.TestErrorResponse;

import reactor.core.publisher.Mono;

public class RequestEmailChangeTests extends AbstractTests {

	private static final String NEW_EMAIL = "new.email@example.com";
	
	private TestEmailForm form() {
		
		TestEmailForm emailForm = new TestEmailForm();
		emailForm.setPassword(USER_PASSWORD);
		emailForm.setNewEmail(NEW_EMAIL);
		
		return emailForm;
	}

	
	@Test
	public void testRequestEmailChange() {
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/email-change-request", UNVERIFIED_USER_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form()), TestEmailForm.class)
		.exchange()
			.expectStatus().isNoContent();

		verify(mailSender).send(any());
		
		User updatedUser = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class);
		Assert.assertEquals(NEW_EMAIL, updatedUser.getNewEmail());
		Assert.assertEquals(UNVERIFIED_USER_EMAIL, updatedUser.getEmail());
	}
	
	
	/**
     * A good admin should be able to request changing email of another user.
     */
	@Test
	public void testGoodAdminRequestEmailChange() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/email-change-request", UNVERIFIED_USER_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(ADMIN_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form()), TestEmailForm.class)
		.exchange()
			.expectStatus().isNoContent();

		User updatedUser = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class);
		Assert.assertEquals(NEW_EMAIL, updatedUser.getNewEmail());
	}	


	/**
     * A request changing email of unknown user.
     */
	@Test
	public void testRequestEmailChangeUnknownUser() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/email-change-request", ObjectId.get())
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(ADMIN_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form()), TestEmailForm.class)
		.exchange()
			.expectStatus().isNotFound();

		verify(mailSender, never()).send(any());
	}
	
	
	/**
	 * A non-admin should not be able to request changing
	 * the email id of another user
	 */
	@Test
	public void testNonAdminRequestEmailChangeAnotherUser() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/email-change-request", ADMIN_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(USER_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form()), TestEmailForm.class)
		.exchange()
			.expectStatus().isForbidden();

		assertNotChanged();
		verify(mailSender, never()).send(any());
	}
	
	
	/**
	 * A bad admin trying to change the email id
	 * of another user
	 */
	@Test
	public void testBadAdminRequestEmailChangeAnotherUser() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/email-change-request", ADMIN_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_ADMIN_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form()), TestEmailForm.class)
		.exchange()
			.expectStatus().isForbidden();
		
		assertNotChanged();
		verify(mailSender, never()).send(any());
	}
	
	
	/**
     * Trying with invalid data.
	 * @throws Exception 
	 * @throws JsonProcessingException 
     */
	@Test
	public void testRequestEmailChangeWithInvalidData() {
		
		// try with null newEmail and password
		tryRequestingEmailChangeBodySpec(new TestEmailForm())
			.consumeWith(errorResponseResult -> {				
				assertErrors(errorResponseResult,
						"emailFormMono.newEmail",
						"emailFormMono.password");
		});		
		assertNotChanged();		
		
    	// try with blank newEmail and password
		TestEmailForm emailForm = new TestEmailForm();
		emailForm.setPassword("");
		emailForm.setNewEmail("");		
		tryRequestingEmailChangeBodySpec(emailForm)
		.consumeWith(errorResponseResult -> {				
			assertErrors(errorResponseResult,
					"emailFormMono.newEmail",
					"emailFormMono.newEmail",
					"emailFormMono.password",
					"emailFormMono.password");
		});

		// try with invalid newEmail
		emailForm = form();
		emailForm.setNewEmail("an-invalid-email");
		tryRequestingEmailChangeBodySpec(emailForm)
		.consumeWith(errorResponseResult -> {				
			assertErrors(errorResponseResult,
					"emailFormMono.newEmail");
		});
		assertNotChanged();

		// try with wrong password
		emailForm = form();
		emailForm.setPassword("wrong-password");
		tryRequestingEmailChangeBodySpec(emailForm)
		.consumeWith(errorResponseResult -> {				
			assertErrors(errorResponseResult,
					"emailFormMono.password");
		});
		assertNotChanged();

		// try with null password
		emailForm = form();
		emailForm.setPassword(null);
		tryRequestingEmailChangeBodySpec(emailForm)
		.consumeWith(errorResponseResult -> {				
			assertErrors(errorResponseResult,
					"emailFormMono.password");
		});
		assertNotChanged();

		// try with an existing email
		emailForm = form();
		emailForm.setNewEmail(ADMIN_EMAIL);;
		tryRequestingEmailChangeBodySpec(emailForm)
		.consumeWith(errorResponseResult -> {				
			assertErrors(errorResponseResult,
					"emailFormMono.newEmail");
		});
		assertNotChanged();
		
		verify(mailSender, never()).send(any());
	}

	
	private BodySpec<TestErrorResponse, ?> tryRequestingEmailChangeBodySpec(TestEmailForm form) {
		
		//@formatter:off
		return CLIENT.post().uri(BASE_URI + "/users/{id}/email-change-request", UNVERIFIED_USER_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form), TestEmailForm.class)
		.exchange()
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
			.expectBody(TestErrorResponse.class);
		//@formatter:on
	}
	
	
	private void assertNotChanged() {
		User updatedUser = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class);
		Assert.assertNull(updatedUser.getNewEmail());
	}
}
