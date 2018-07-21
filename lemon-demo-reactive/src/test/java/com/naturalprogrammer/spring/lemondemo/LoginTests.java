package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_PASSWORD;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.CLIENT;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import org.junit.Test;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;

public class LoginTests extends AbstractTests {

	@Test
	public void testLogin() {
		
		testUtils.loginResponse(ADMIN_EMAIL, ADMIN_PASSWORD)
			.expectStatus().isOk()
			.expectBody(TestUserDto.class)
			.consumeWith(result -> {
				
				TestUserDto user = result.getResponseBody();
				
				assertEquals(ADMIN_ID, user.getId());
				assertNull(user.getPassword());
				assertEquals("admin@example.com", user.getUsername());
				assertEquals(1, user.getRoles().size());
				assertTrue(user.getRoles().contains("ADMIN"));
				assertEquals("Admin 1", user.getTag().getName());
				assertFalse(user.isUnverified());
				assertFalse(user.isBlocked());
				assertTrue(user.isAdmin());
				assertTrue(user.isGoodUser());
				assertTrue(user.isGoodAdmin());				
			});
	}
	
	
	@Test
	public void testLoginTokenExpiry() throws Exception {
		
		String token = login(ADMIN_EMAIL, ADMIN_PASSWORD, 500L);
		Thread.sleep(501L);
		
		CLIENT.get()
			.uri("/api/core/ping")
			.header(LecUtils.TOKEN_REQUEST_HEADER_NAME, token)
			.exchange()
			.expectStatus().isUnauthorized();
	}


	private String login(String username, String password, long expirationMillis) {
		
    	return CLIENT.post()
                .uri(BASE_URI + "/login")
                .body(fromFormData("username", username)
                		     .with("password", password)
                		     .with("expirationMillis", Long.toString(expirationMillis)))
                .exchange()
            	.returnResult(TestUserDto.class)
            	.getResponseHeaders()
            	.getFirst(LecUtils.TOKEN_RESPONSE_HEADER_NAME);
	}
}
