package com.naturalprogrammer.spring.lemon;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.SignupInput;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.forms.NonceForm;
import com.naturalprogrammer.spring.lemon.security.JwtService;
import com.naturalprogrammer.spring.lemon.security.SpringUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * The Lemon API
 * 
 * @author Sanjay Patel
 *
 * @param <U>	The User class
 * @param <ID>	The Primary key type of User class 
 */
public abstract class LemonController
	<U extends AbstractUser<U,ID>, ID extends Serializable> {

	private static final Log log = LogFactory.getLog(LemonController.class);

    private long jwtExpirationMilli;
    private JwtService jwtService;
	private LemonService<U, ID> lemonService;
	
	@Autowired
	public void createLemonController(
			LemonProperties properties,
			LemonService<U, ID> lemonService,
			JwtService jwtService) {
		
		this.jwtExpirationMilli = properties.getJwt().getExpirationMilli();
		this.lemonService = lemonService;
		this.jwtService = jwtService;
		
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
	 * Returns context properties needed at the client side, and
	 * the current-user data.
	 */
	@GetMapping(value = "/context", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Map<String, Object> getContext() {
		
		Map<String, Object> context =
			LemonUtils.mapOf("context", lemonService.getContext(),
							"user", LemonUtils.getSpringUser());
		
		log.debug("Returning context: " + context);

		return context;
	}
	

	/**
	 * Signs up a user, and logs him in.
     *
	 * @param user	data fed by the user
	 * @return data about the logged in user
	 */
	@PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public SpringUser<ID> signup(@RequestBody @JsonView(SignupInput.class) U user,
			HttpServletResponse response) {
		
		log.debug("Signing up: " + user);
		lemonService.signup(user);
		log.debug("Signed up: " + user);

		return springUserWithToken(response);
	}
	
	
	/**
	 * Resends verification mail. 
	 */
	@GetMapping("/users/{id}/resend-verification-mail")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resendVerificationMail(@PathVariable("id") U user) {
		
		log.debug("Resending verification mail for: " + user);
		lemonService.resendVerificationMail(user);
		log.debug("Resent verification mail for: " + user);
	}	


	/**
	 * Verifies current-user.
	 */
	@PostMapping(value = "/users/{userId}/verification", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public SpringUser<ID> verifyUser(
			@PathVariable ID userId,
			@RequestParam String code,
			HttpServletResponse response) {
		
		log.debug("Verifying user ...");		
		lemonService.verifyUser(userId, code);
		
		return springUserWithToken(response);
	}
	

	/**
	 * The forgot Password feature.
	 * @throws MessagingException 
	 */
	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void forgotPassword(@RequestParam String email) throws MessagingException {
		
		log.debug("Received forgot password request for: " + email);				
		lemonService.forgotPassword(email);
	}
	

	/**
	 * Resets password after it is forgotten.
	 * @return 
	 */
	@PostMapping("/users/{userId}/reset-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SpringUser<ID> resetPassword(@PathVariable ID userId,
							  @RequestParam String code,
							  @RequestParam String newPassword,
							  HttpServletResponse response) {
		
		log.debug("Resetting password ... ");				
		lemonService.resetPassword(userId, code, newPassword);
		
		return springUserWithToken(response);
	}


	/**
	 * Fetches a user by email.
	 */
	@GetMapping(value = "/users/fetch-by-email", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public U fetchUserByEmail(@RequestParam String email) {
		
		log.debug("Fetching user by email: " + email);						
		return lemonService.fetchUserByEmail(email);
	}

	
	/**
	 * Fetches a user by Id.
	 */	
	@GetMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public U fetchUserById(@PathVariable("id") U user) {
		
		log.debug("Fetching user: " + user);				
		return lemonService.processUser(user);
	}

	
	/**
	 * Updates a user.
	 * @throws JsonPatchException 
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	@PatchMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public SpringUser<ID> updateUser(
			@PathVariable("id") U user,
			@RequestBody String patch,
			HttpServletResponse response)
			throws JsonProcessingException, IOException, JsonPatchException {
		
		log.debug("Updating user ... ");
		
		// ensure that the user exists
		LemonUtils.validateFound(user);
		U updatedUser = LemonUtils.applyPatch(user, patch); // create a patched form
		lemonService.updateUser(user, updatedUser);
		
		// return the currently logged in user data (in case updated)
		return springUserWithToken(response);		
	}
	
	
	/**
	 * Changes password.
	 */
	@PostMapping("/users/{id}/change-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@PathVariable("id") U user,
			@RequestBody ChangePasswordForm changePasswordForm,
			HttpServletResponse response) {
		
		log.debug("Changing password ... ");				
		String username = lemonService.changePassword(user, changePasswordForm);
		
		jwtService.addAuthHeader(response, username, jwtExpirationMilli);
	}


	/**
	 * Requests for changing email.
	 */
	@PostMapping("/users/{id}/request-email-change")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void requestEmailChange(@PathVariable("id") U user,
								   @RequestBody U updatedUser) {
		
		log.debug("Requesting email change ... ");				
		lemonService.requestEmailChange(user, updatedUser);
	}


	/**
	 * Changes the email.
	 * @return 
	 */
	@PostMapping("/users/{userId}/change-email")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public SpringUser<ID> changeEmail(
			@PathVariable ID userId,
			@RequestParam String code,
			HttpServletResponse response) {
		
		log.debug("Changing email of user ...");		
		lemonService.changeEmail(userId, code);
		
		// return the currently logged in user with new email
		return springUserWithToken(response);		
	}
	
	/**
	 * Login with nonce - used after a user social logs in
	 */
	@PostMapping("/login-with-nonce")
	public SpringUser<ID> loginWithNonce(@RequestBody NonceForm<ID> nonce, HttpServletResponse response) {
		
		log.debug("Logging in user in exchange of nonce ... ");
		lemonService.loginWithNonce(nonce, response);
		
		SpringUser<ID> springUser = LemonUtils.getSpringUser();
		
		if (nonce.getExpirationMilli() == null)
			nonce.setExpirationMilli(jwtExpirationMilli);
		
		jwtService.addAuthHeader(response,
				springUser.getUsername(),
				nonce.getExpirationMilli());

		return springUser;
	}
	
	/**
	 * Fetch a new token - for session scrolling, switch user etc.
	 */
	@PostMapping("/fetch-new-token")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void fetchNewToken(@RequestParam Optional<Long> expirationMillis,
			@RequestParam Optional<String> username,
			HttpServletResponse response) {
		
		log.debug("Logging in user in exchange of nonce ... ");
		lemonService.fetchNewToken(expirationMillis, username, response);
	}

	
	protected SpringUser<ID> springUserWithToken(HttpServletResponse response) {
		
		SpringUser<ID> springUser = LemonUtils.getSpringUser();
		jwtService.addAuthHeader(response, springUser.getUsername(), jwtExpirationMilli);
		return springUser;
	}
}
