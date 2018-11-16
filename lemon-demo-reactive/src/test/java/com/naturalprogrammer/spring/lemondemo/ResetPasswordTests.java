package com.naturalprogrammer.spring.lemondemo;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_EMAIL;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.ADMIN_ID;
import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.CLIENT;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemondemo.dto.TestResetPasswordForm;

import reactor.core.publisher.Mono;

public class ResetPasswordTests extends AbstractTests {

	private String forgotPasswordCode;
	
	@Autowired
	private GreenTokenService greenTokenService;
	
	@Before
	public void setUp() {
		
		forgotPasswordCode = greenTokenService.createToken(
				GreenTokenService.FORGOT_PASSWORD_AUDIENCE,
				ADMIN_EMAIL, 60000L);
	}
	
	@Test
	public void testResetPassword() throws Exception {
		
		final String NEW_PASSWORD = "newPassword!";
		
		resetPassword(forgotPasswordCode, NEW_PASSWORD)
			.expectStatus().isOk()
			.expectHeader().exists(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
			.expectBody().jsonPath("$.id").isEqualTo(ADMIN_ID.toString());
	
		// Ensure able to login with new password
		testUtils.login(ADMIN_EMAIL, NEW_PASSWORD);

	    // Repeating shouldn't work
		resetPassword(forgotPasswordCode, NEW_PASSWORD)
			.expectStatus().isUnauthorized();
	}
	
	@Test
	public void testResetPasswordInvalidData() throws Exception {
		
		// Wrong code
		resetPassword("wrong-code", "abc99!")
			.expectStatus().isUnauthorized();

		// Blank password
		resetPassword(forgotPasswordCode, "")
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

		// Invalid password
		resetPassword(forgotPasswordCode, "abc")
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	private ResponseSpec resetPassword(String forgotPasswordCode, String newPassword) {
		
		return CLIENT.post().uri(BASE_URI + "/reset-password")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(form(forgotPasswordCode, newPassword)), TestResetPasswordForm.class)
		.exchange();
	}

	private TestResetPasswordForm form(String code, String newPassword) {
		
		TestResetPasswordForm form = new TestResetPasswordForm();
		form.setCode(code);
		form.setNewPassword(newPassword);
		
		return form;
	}
}
