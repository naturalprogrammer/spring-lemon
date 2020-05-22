package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemondemo.dto.TestToken;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.*;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.junit.Assert.assertNotNull;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

public class FetchNewTokenTests extends AbstractTests {

	
	@Test
	public void testFetchNewToken() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/fetch-new-auth-token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
			.exchange()
			.expectStatus().isOk()
			.expectBody(TestToken.class)
			.consumeWith(this::ensureTokenWorks);
	}

	
	@Test
	public void testFetchNewTokenExpiration() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/fetch-new-auth-token")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
		.body(fromFormData("expirationMillis", "1000"))
		.exchange()
		.expectStatus().isOk()
		.expectBody(TestToken.class)
		.consumeWith(result -> {
			
			ensureTokenWorks(result);
			TestToken token = result.getResponseBody();
			
			try {
				Thread.sleep(1001L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			CLIENT.get()
				.uri(BASE_URI + "/context")
					.header(HttpHeaders.AUTHORIZATION, token.getToken())
					.exchange()
				.expectStatus().isUnauthorized();			
		});
	}
	
	@Test
	public void testFetchNewTokenByAdminForAnotherUser() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/fetch-new-auth-token")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.header(HttpHeaders.AUTHORIZATION, TOKENS.get(ADMIN_ID))
		.body(fromFormData("username", UNVERIFIED_USER_EMAIL))
		.exchange()
		.expectStatus().isOk()
		.expectBody(TestToken.class)
		.consumeWith(this::ensureTokenWorks);
	}
	
	
	@Test
	public void testFetchNewTokenByNonAdminForAnotherUser() throws Exception {
		
		CLIENT.post().uri(BASE_URI + "/fetch-new-auth-token")
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.header(HttpHeaders.AUTHORIZATION, TOKENS.get(UNVERIFIED_USER_ID))
		.body(fromFormData("username", ADMIN_EMAIL))
		.exchange()
		.expectStatus().isForbidden()
		.expectBody().jsonPath("$.token").doesNotExist();
	}

	
	private void ensureTokenWorks(EntityExchangeResult<TestToken> result) {
		
		TestToken token = result.getResponseBody();
		assertNotNull(token.getToken());

		testUtils.contextResponse(token.getToken())
			.expectBody()
			.jsonPath("$.user.id").isEqualTo(UNVERIFIED_USER_ID.toString());
	}
}
