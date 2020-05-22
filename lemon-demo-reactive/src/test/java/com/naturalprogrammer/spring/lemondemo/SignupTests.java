package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemondemo.dto.TestErrorResponse;
import com.naturalprogrammer.spring.lemondemo.dto.TestLemonFieldError;
import com.naturalprogrammer.spring.lemondemo.dto.TestUser;
import com.naturalprogrammer.spring.lemondemo.dto.TestUserDto;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.*;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class SignupTests extends AbstractTests {

	@Test
	public void testSignup() throws Exception {
		
		signup("user.foo@example.com", "user123", "User Foo")
		.expectStatus().isCreated()
		.expectHeader().exists(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
		.expectBody(TestUserDto.class)
		.consumeWith(result -> {			
			TestUserDto userDto = result.getResponseBody();
			assertNotNull(userDto.getId());
			assertNull(userDto.getPassword());
			assertEquals("user.foo@example.com", userDto.getUsername());
			assertEquals(1, userDto.getRoles().size());
			assertTrue(userDto.getRoles().contains("UNVERIFIED"));
			assertEquals("User Foo", userDto.getTag().getName());
			assertTrue(userDto.isUnverified());
			assertFalse(userDto.isBlocked());
			assertFalse(userDto.isAdmin());
			assertFalse(userDto.isGoodUser());
			assertFalse(userDto.isGoodAdmin());
		});
				
		verify(mailSender).send(any());

		// Ensure that password got encrypted
		assertNotEquals("user123",
			mongoTemplate.findOne(query(where("email").is("user.foo@example.com")),
					User.class).block().getPassword());
	}

	@Test
	public void testSignupWithInvalidData() throws Exception {
		
		signup("abc", "user1", null)
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
			.expectBody(TestErrorResponse.class)
			.consumeWith(errorResponseResult -> {				
				assertErrors(errorResponseResult,
						"userMono.email",
						"userMono.email",
						"userMono.password",
						"userMono.name");
				
				Collection<TestLemonFieldError> errors = errorResponseResult.getResponseBody().getErrors();
				assertTrue(errors.stream()
						.map(TestLemonFieldError::getCode).collect(Collectors.toSet())
						.containsAll(Arrays.asList(
								"NotBlank",
								"Size",
								"Email")));
				
				assertTrue(errors.stream()
						.map(TestLemonFieldError::getMessage).collect(Collectors.toSet())
						.containsAll(Arrays.asList(
								"Not a well formed email address",
								"Name required",
								"Email must be between 4 and 250 characters",
								"Password must be between 6 and 50 characters")));
			});
		
		verify(mailSender, never()).send(any());
	}
	
	@Test
	public void testSignupDuplicateEmail() throws Exception {
		
		signup(ADMIN_EMAIL, "user123", "User Foo")
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

		verify(mailSender, never()).send(any());
	}

	private ResponseSpec signup(String email, String password, String name) {
		
		TestUser user = new TestUser(email, password, name);
		
		return CLIENT.post().uri(BASE_URI + "/users", UNVERIFIED_USER_ID)
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(user), TestUser.class)
		.exchange();
	}

}
