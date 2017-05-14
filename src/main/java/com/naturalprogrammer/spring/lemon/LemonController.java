package com.naturalprogrammer.spring.lemon;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
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

	private LemonService<U, ID> lemonService;
	
	@Autowired
	public void setLemonController(LemonService<U, ID> lemonService) {
		
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
	 * A simple function for pinging this server.
	 */
	@GetMapping("/ping-session")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void ping(HttpSession session) {
		log.debug("Pinging session. Session id: " + session.getId());
	}


	/**
	 * Returns context properties needed at the client side, and
	 * the current-user data.
	 */
	@GetMapping(value = "/context", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Map<String, Object> getContext() {
		
		Map<String, Object> context =
			LemonUtils.mapOf("context", lemonService.getContext(),
							"user", lemonService.userForClient());
		
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
	public U signup(@RequestBody @JsonView(SignupInput.class) U user) {
		
		log.debug("Signing up: " + user);
		lemonService.signup(user);
		log.debug("Signed up: " + user);
		
		return lemonService.userForClient();
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
	@PostMapping(value = "/users/{verificationCode}/verify", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public U verifyUser(@PathVariable String verificationCode) {
		
		log.debug("Verifying user ...");		
		lemonService.verifyUser(verificationCode);
		
		return lemonService.userForClient();
	}
	

	/**
	 * The forgot Password feature.
	 */
	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void forgotPassword(@RequestParam String email) {
		
		log.debug("Received forgot password request for: " + email);				
		lemonService.forgotPassword(email);
	}
	

	/**
	 * Resets password after it is forgotten.
	 */
	@PostMapping("/users/{forgotPasswordCode}/reset-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetPassword(@PathVariable String forgotPasswordCode,
							  @RequestParam String newPassword) {
		
		log.debug("Resetting password ... ");				
		lemonService.resetPassword(forgotPasswordCode, newPassword);
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
	public U updateUser(@PathVariable("id") U user, @RequestBody String patch)
			throws JsonProcessingException, IOException, JsonPatchException {
		
		log.debug("Updating user ... ");
		
		// ensure that the user exists
		LemonUtils.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		
		U updatedUser = LemonUtils.applyPatch(user, patch); // create a patched form
		lemonService.updateUser(user, updatedUser);
		
		// return the currently logged in user data (in case updated)
		return lemonService.userForClient();		
	}
	
	
	/**
	 * Changes password.
	 */
	@PostMapping("/users/{id}/change-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@PathVariable("id") U user,
			@RequestBody ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password ... ");				
		lemonService.changePassword(user, changePasswordForm);
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
	 */
	@PostMapping("/users/{changeEmailCode}/change-email")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changeEmail(@PathVariable String changeEmailCode) {
		
		log.debug("Changing email of user ...");		
		lemonService.changeEmail(changeEmailCode);
	}
	
	@PostMapping("/users/{id}/api-key")
	public Map<String, String> createApiKey(@PathVariable("id") U user) {
		
		log.debug("Creating API Key ... ");				
		return lemonService.createApiKey(user);
	}
	
	@DeleteMapping("/users/{id}/api-key")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeApiKey(@PathVariable("id") U user) {
		
		log.debug("Removing API Key ... ");				
		lemonService.removeApiKey(user);
	}
}
