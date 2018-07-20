package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginTests extends AbstractTests {

	@Test
	public void testLogin() throws Exception {
		
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
}
