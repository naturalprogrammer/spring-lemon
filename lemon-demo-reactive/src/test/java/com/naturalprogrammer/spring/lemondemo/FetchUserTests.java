package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.CLIENT;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.TOKENS;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.UNVERIFIED_USER_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.UNVERIFIED_USER_ID;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class FetchUserTests extends AbstractTests {

	@Test
	public void testFetchUserById() throws Exception {
		
		CLIENT.get().uri(BASE_URI + "/users/{id}", ADMIN_ID)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
			.jsonPath("$.id").isEqualTo(ADMIN_ID.toString())
			.jsonPath("$.email").doesNotExist()
			.jsonPath("$.password").doesNotExist()
			.jsonPath("$.credentialsUpdatedAt").doesNotExist()
			.jsonPath("$.name").isEqualTo("Admin 1");
	}

	@Test
	public void testFetchUserByIdLoggedIn() throws Exception {
		
		// Same user logged in
		CLIENT.get().uri(BASE_URI + "/users/{id}", ADMIN_ID)
		.header(HttpHeaders.AUTHORIZATION, TOKENS.get(ADMIN_ID))
		.exchange()
		.expectStatus().isOk()
		.expectBody()
			.jsonPath("$.id").isEqualTo(ADMIN_ID.toString())
			.jsonPath("$.email").isEqualTo(ADMIN_EMAIL)
			.jsonPath("$.password").doesNotExist()
			.jsonPath("$.credentialsUpdatedAt").doesNotExist()
			.jsonPath("$.name").isEqualTo("Admin 1");

		// Another user logged in
		CLIENT.get().uri(BASE_URI + "/users/{id}", ADMIN_ID)
		.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
		.exchange()
		.expectStatus().isOk()
		.expectBody()
			.jsonPath("$.id").isEqualTo(ADMIN_ID.toString())
			.jsonPath("$.email").doesNotExist();
		
		// Admin user logged in - fetching another user
		CLIENT.get().uri(BASE_URI + "/users/{id}", UNVERIFIED_USER_ID)
		.header(HttpHeaders.AUTHORIZATION, TOKENS.get(ADMIN_ID))
		.exchange()
		.expectStatus().isOk()
		.expectBody()
			.jsonPath("$.id").isEqualTo(UNVERIFIED_USER_ID.toString())
			.jsonPath("$.email").isEqualTo(UNVERIFIED_USER_EMAIL);
	}

	@Test
	public void testFetchNonExistingUserById() throws Exception {
		
		CLIENT.get().uri(BASE_URI + "/users/{id}", ObjectId.get())
		.exchange()
		.expectStatus().isNotFound();
	}
	
	@Test
	public void testFetchUserByEmail() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/users/fetch-by-email")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.body(fromFormData("email", ADMIN_EMAIL))
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.id").isEqualTo(ADMIN_ID.toString())
		.jsonPath("$.password").doesNotExist()
		.jsonPath("$.credentialsUpdatedAt").doesNotExist()
		.jsonPath("$.name").isEqualTo("Admin 1");
	}

	@Test
	public void testFetchUserByInvalidEmail() throws Exception {
		
		// email does not exist
		CLIENT.post().uri(BASE_URI + "/users/fetch-by-email")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.body(fromFormData("email", "foo@example.com"))
		.exchange()
		.expectStatus().isNotFound();

		// Blank email
		CLIENT.post().uri(BASE_URI + "/users/fetch-by-email")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.body(fromFormData("email", ""))
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

		// Invalid email
		CLIENT.post().uri(BASE_URI + "/users/fetch-by-email")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.body(fromFormData("email", "invalid-email"))
		.exchange()
		.expectStatus().isNotFound();
	}
}
