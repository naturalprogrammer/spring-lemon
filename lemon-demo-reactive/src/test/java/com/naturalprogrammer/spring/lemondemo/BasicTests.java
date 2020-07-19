package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import org.junit.jupiter.api.Test;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.*;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;

class BasicTests extends AbstractTests {

	@Test
	void testPing() throws Exception {
		
		CLIENT.get()
			.uri(BASE_URI + "/ping")
			.exchange()
			.expectStatus()
				.isNoContent();
	}

	@Test
	void testGetContextLoggedIn() throws Exception {
		
		testUtils.contextResponse(TOKENS.get(ADMIN_ID))
				.expectHeader().exists(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
				.expectBody()
					.jsonPath("$.context.reCaptchaSiteKey").exists()
					.jsonPath("$.user.id").isEqualTo(ADMIN_ID.toString())
					.jsonPath("$.user.roles[0]").isEqualTo("ADMIN")
					.jsonPath("$.user.password").doesNotExist();
	}
	
	@Test
	void testGetContextWithoutLoggedIn() throws Exception {
		
		CLIENT.get()
		.uri(BASE_URI + "/context")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().doesNotExist(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
			.expectBody()
				.jsonPath("$.context.reCaptchaSiteKey").exists()
				.jsonPath("$.user").doesNotExist();
	}	

}
