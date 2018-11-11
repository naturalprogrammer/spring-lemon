package com.naturalprogrammer.spring.lemon;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

/**
 * The Lemon API. See the
 * <a href="https://github.com/naturalprogrammer/spring-lemon#documentation-and-resources">
 * API documentation</a> for details.
 * 
 * @author Sanjay Patel
 */
public abstract class LemonController
	<U extends AbstractUser<ID>, ID extends Serializable> {

	private static final Log log = LogFactory.getLog(LemonController.class);

    private long jwtExpirationMillis;
	private LemonService<U, ID> lemonService;
	
	@Autowired
	public void createLemonController(
			LemonProperties properties,
			LemonService<U, ID> lemonService) {
		
		this.jwtExpirationMillis = properties.getJwt().getExpirationMillis();
		this.lemonService = lemonService;
		
		log.info("Created");
	}


	/**
	 * A simple function for pinging this server.
	 */
	@GetMapping("/ping")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void ping() {
		
		log.debug("Received a ping");
	}
	
	
	/**
	 * Returns context properties needed at the client side,
	 * current-user data and an Authorization token as a response header.
	 */
	@GetMapping("/context")
	public Map<String, Object> getContext(
			@RequestParam Optional<Long> expirationMillis,
			HttpServletResponse response) {

		log.debug("Getting context ");
		Map<String, Object> context = lemonService.getContext(expirationMillis, response);
		log.debug("Returning context: " + context);

		return context;
	}
	

	/**
	 * Signs up a user, and
	 * returns current-user data and an Authorization token as a response header.
	 */
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto signup(@RequestBody @JsonView(UserUtils.SignupInput.class) U user,
			HttpServletResponse response) {
		
		log.debug("Signing up: " + user);
		lemonService.signup(user);
		log.debug("Signed up: " + user);

		return userWithToken(response);
	}
	
	
	/**
	 * Resends verification mail
	 */
	@PostMapping("/users/{id}/resend-verification-mail")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resendVerificationMail(@PathVariable("id") U user) {
		
		log.debug("Resending verification mail for: " + user);
		lemonService.resendVerificationMail(user);
		log.debug("Resent verification mail for: " + user);
	}	


	/**
	 * Verifies current-user
	 */
	@PostMapping("/users/{id}/verification")
	public UserDto verifyUser(
			@PathVariable ID id,
			@RequestParam String code,
			HttpServletResponse response) {
		
		log.debug("Verifying user ...");		
		lemonService.verifyUser(id, code);
		
		return userWithToken(response);
	}
	

	/**
	 * The forgot Password feature
	 */
	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void forgotPassword(@RequestParam String email) {
		
		log.debug("Received forgot password request for: " + email);				
		lemonService.forgotPassword(email);
	}
	

	/**
	 * Resets password after it's forgotten
	 */
	@PostMapping("/reset-password")
	public UserDto resetPassword(
			@RequestBody ResetPasswordForm form,
			HttpServletResponse response) {
		
		log.debug("Resetting password ... ");				
		lemonService.resetPassword(form);
		
		return userWithToken(response);
	}


	/**
	 * Fetches a user by email
	 */
	@PostMapping("/users/fetch-by-email")
	public U fetchUserByEmail(@RequestParam String email) {
		
		log.debug("Fetching user by email: " + email);						
		return lemonService.fetchUserByEmail(email);
	}

	
	/**
	 * Fetches a user by ID
	 */	
	@GetMapping("/users/{id}")
	public U fetchUserById(@PathVariable("id") U user) {
		
		log.debug("Fetching user: " + user);				
		return lemonService.processUser(user);
	}

	
	/**
	 * Updates a user
	 */
	@PatchMapping("/users/{id}")
	public UserDto updateUser(
			@PathVariable("id") U user,
			@RequestBody String patch,
			HttpServletResponse response)
			throws JsonProcessingException, IOException, JsonPatchException {
		
		log.debug("Updating user ... ");
		
		// ensure that the user exists
		LexUtils.ensureFound(user);
		U updatedUser = LecUtils.applyPatch(user, patch); // create a patched form
		UserDto userDto = lemonService.updateUser(user, updatedUser);
		
		// Send a new token for logged in user in the response
		userWithToken(response);
		
		// Send updated user data in the response
		return userDto;
	}
	
	
	/**
	 * Changes password
	 */
	@PostMapping("/users/{id}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@PathVariable("id") U user,
			@RequestBody ChangePasswordForm changePasswordForm,
			HttpServletResponse response) {
		
		log.debug("Changing password ... ");				
		String username = lemonService.changePassword(user, changePasswordForm);
		
		lemonService.addAuthHeader(response, username, jwtExpirationMillis);
	}


	/**
	 * Requests for changing email
	 */
	@PostMapping("/users/{id}/email-change-request")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void requestEmailChange(@PathVariable("id") U user,
								   @RequestBody U updatedUser) {
		
		log.debug("Requesting email change ... ");				
		lemonService.requestEmailChange(user, updatedUser);
	}


	/**
	 * Changes the email
	 */
	@PostMapping("/users/{userId}/email")
	public UserDto changeEmail(
			@PathVariable ID userId,
			@RequestParam String code,
			HttpServletResponse response) {
		
		log.debug("Changing email of user ...");		
		lemonService.changeEmail(userId, code);
		
		// return the currently logged in user with new email
		return userWithToken(response);		
	}


	/**
	 * Fetch a new token - for session sliding, switch user etc.
	 */
	@PostMapping("/fetch-new-auth-token")
	public Map<String, String> fetchNewToken(
			@RequestParam Optional<Long> expirationMillis,
			@RequestParam Optional<String> username,
			HttpServletResponse response) {
		
		log.debug("Fetching a new token ... ");
		return LecUtils.mapOf("token", lemonService.fetchNewToken(expirationMillis, username));
	}


	/**
	 * Fetch a self-sufficient token with embedded UserDto - for interservice communications
	 */
	@GetMapping("/fetch-full-token")
	public Map<String, String> fetchFullToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		
		log.debug("Fetching a micro token");
		return lemonService.fetchFullToken(authHeader);
	}	

	
	/**
	 * returns the current user and a new authorization token in the response
	 */
	protected UserDto userWithToken(HttpServletResponse response) {
		
		UserDto currentUser = LecwUtils.currentUser();
		lemonService.addAuthHeader(response, currentUser.getUsername(), jwtExpirationMillis);
		return currentUser;
	}
}
