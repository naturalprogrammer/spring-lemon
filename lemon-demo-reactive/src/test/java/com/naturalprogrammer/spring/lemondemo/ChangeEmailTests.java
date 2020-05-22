package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemondemo.domain.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.*;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

public class ChangeEmailTests extends AbstractTests {

	private static final String NEW_EMAIL = "new.email@example.com";

	private String changeEmailCode;
	
	@Autowired
	private GreenTokenService greenTokenService;
	
	@Before
	public void setUp() {
		
		User user = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class).block();
		user.setNewEmail(NEW_EMAIL);
		mongoTemplate.save(user).block();
		
		changeEmailCode = greenTokenService.createToken(
				GreenTokenService.CHANGE_EMAIL_AUDIENCE,
				UNVERIFIED_USER_ID.toString(), 60000L,
				LecUtils.mapOf("newEmail", NEW_EMAIL));
	}
	
	@Test
	public void testChangeEmail() throws Exception {
		
		changeEmailResponse(changeEmailCode)
        	.expectStatus().isOk()
        	.expectHeader().valueMatches(LecUtils.TOKEN_RESPONSE_HEADER_NAME, ".*\\..*")
        	.expectBody().jsonPath("$.id").isEqualTo(UNVERIFIED_USER_ID.toString());

		User updatedUser = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class).block();
		Assert.assertNull(updatedUser.getNewEmail());
		Assert.assertEquals(NEW_EMAIL, updatedUser.getEmail());
		
		changeEmailResponse(changeEmailCode)
    		.expectStatus().isUnauthorized();
	}
	
    /**
     * Providing a wrong changeEmailCode shouldn't work.
     */
	@Test
	public void testChangeEmailWrongCode() throws Exception {
		
		// Blank token
		changeEmailResponse("")
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

		// Wrong audience
		String code = greenTokenService.createToken(
				"", // blank audience
				UNVERIFIED_USER_ID.toString(), 60000L,
				LecUtils.mapOf("newEmail", NEW_EMAIL));
		
		changeEmailResponse(code)
			.expectStatus().isUnauthorized();

		// Wrong userId subject
		code = greenTokenService.createToken(
				GreenTokenService.CHANGE_EMAIL_AUDIENCE,
				ADMIN_ID.toString(), 60000L,
				LecUtils.mapOf("newEmail", NEW_EMAIL));
		
		changeEmailResponse(code)
			.expectStatus().isForbidden();
		
		// Wrong new email
		code = greenTokenService.createToken(
				GreenTokenService.CHANGE_EMAIL_AUDIENCE,
				UNVERIFIED_USER_ID.toString(), 60000L,
				LecUtils.mapOf("newEmail", "wrong.new.email@example.com"));
		
		changeEmailResponse(code)
		.expectStatus().isForbidden();
	}

    /**
     * Providing a wrong changeEmailCode shouldn't work.
     */
	@Test
	public void testChangeEmailObsoleteCode() throws Exception {
		
		// credentials updated after the request for email change was made
		Thread.sleep(1L);
		User user = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class).block();
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		mongoTemplate.save(user).block();
		
		// A new auth token is needed, because old one would be obsolete!
		String authToken = testUtils.login(UNVERIFIED_USER_EMAIL, USER_PASSWORD);
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/email", UNVERIFIED_USER_ID)
			.header(HttpHeaders.AUTHORIZATION, authToken)
	        .body(fromFormData("code", changeEmailCode))
	        .exchange()
				.expectStatus().isUnauthorized();
	}
	
	/**
     * Trying without having requested first.
	 * @throws Exception 
     */
	@Test
	public void testChangeEmailWithoutAnyRequest() throws Exception {

		CLIENT.post().uri(BASE_URI + "/users/{id}/email", USER_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(USER_ID))
	        .body(fromFormData("code", changeEmailCode))
        .exchange()
        	.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
	}
		
    /**
     * Trying after some user registers the newEmail, leaving it non unique.
     * @throws Exception 
     */
	@Test
	public void testChangeEmailNonUniqueEmail() throws Exception {
		
		// Some other user changed to the same email
		User user = mongoTemplate.findById(ADMIN_ID, User.class).block();
		user.setEmail(NEW_EMAIL);
		mongoTemplate.save(user).block();
		
		// Blank token
		changeEmailResponse(changeEmailCode)
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	private ResponseSpec changeEmailResponse(String code) {
		
		return CLIENT.post().uri(BASE_URI + "/users/{id}/email", UNVERIFIED_USER_ID)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
	        .body(fromFormData("code", code))
        .exchange();
	}

}
