package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

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
	public void setLemonService(LemonService<U, ID> lemonService) {
		this.lemonService = lemonService;
	}


	/**
	 * A simple function for pinging this server.
	 */
	@RequestMapping(value="/ping", method=RequestMethod.GET)
	public void ping() {
		log.debug("Received a ping");
	}
	

	/**
	 * Returns context properties needed at the client side, and
	 * the current-user data.
	 */
	@RequestMapping(value="/context", method=RequestMethod.GET)
	public Map<String, Object> getContext() {
		
		Map<String, Object> context =
			LemonUtil.mapOf("context", lemonService.getContext(),
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
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public U signup(@RequestBody U user) {
		
		log.debug("Signing up: " + user);
		lemonService.signup(user);
		log.debug("Signed up: " + user);
		
		return lemonService.userForClient();
	}
	
	
	/**
	 * Resends verification mail. 
	 */
	@RequestMapping(value="/users/{id}/resend-verification-mail",
			        method=RequestMethod.GET)
	public void resendVerificationMail(@PathVariable("id") U user) {
		
		log.debug("Resending verification mail for: " + user);
		lemonService.resendVerificationMail(user);
		log.debug("Resent verification mail for: " + user);
	}	


	/**
	 * Verifies current-user.
	 */
	@RequestMapping(value="/users/{verificationCode}/verify",
					method=RequestMethod.POST)
	public U verifyUser(@PathVariable String verificationCode) {
		
		log.debug("Verifying user ...");		
		lemonService.verifyUser(verificationCode);
		
		return lemonService.userForClient();
	}
	

	/**
	 * The forgot Password feature.
	 */
	@RequestMapping(value="/forgot-password", method=RequestMethod.POST)
	public void forgotPassword(@RequestParam String email) {
		
		log.debug("Received forgot password request for: " + email);				
		lemonService.forgotPassword(email);
	}
	

	/**
	 * Resets password after it is forgotten.
	 */
	@RequestMapping(value="/users/{forgotPasswordCode}/reset-password",
					method=RequestMethod.POST)
	public void resetPassword(@PathVariable String forgotPasswordCode,
							  @RequestParam String newPassword) {
		
		log.debug("Resetting password ... ");				
		lemonService.resetPassword(forgotPasswordCode, newPassword);
	}


	/**
	 * Fetches a user by email.
	 */
	@RequestMapping(value="/users/fetch-by-email", method=RequestMethod.GET)
	public U fetchUserByEmail(@RequestParam String email) {
		
		log.debug("Fetching user by email: " + email);						
		return lemonService.fetchUserByEmail(email);
	}

	
	/**
	 * Fetches a user by Id.
	 */	
	@RequestMapping(value="/users/{id}/fetch-by-id", method=RequestMethod.GET)
	public U fetchUserById(@PathVariable("id") U user) {
		
		log.debug("Fetching user: " + user);				
		return lemonService.processUser(user);
	}

	
	/**
	 * Updates a user.
	 */
	@RequestMapping(value="/users/{id}/update", method=RequestMethod.POST)
	public U updateUser(@PathVariable("id") U user, @RequestBody U updatedUser) {
		
		log.debug("Updating user ... ");				
		lemonService.updateUser(user, updatedUser);
		return lemonService.userForClient();		
	}
	
	
	/**
	 * Changes password.
	 */
	@RequestMapping(value="/users/{id}/change-password",
					method=RequestMethod.POST)
	public void changePassword(@PathVariable("id") U user,
			@RequestBody ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password ... ");				
		lemonService.changePassword(user, changePasswordForm);
	}


	/**
	 * Requests for changing email.
	 */
	@RequestMapping(value="/users/{id}/request-email-change",
					method=RequestMethod.POST)
	public void requestEmailChange(@PathVariable("id") U user,
								   @RequestBody U updatedUser) {
		
		log.debug("Requesting email change ... ");				
		lemonService.requestEmailChange(user, updatedUser);
	}
	
	/**
	 * Changes the email.
	 */
	@RequestMapping(value="/users/{changeEmailCode}/change-email",
					method=RequestMethod.POST)
	public void changeEmail(@PathVariable String changeEmailCode) {
		
		log.debug("Changing email of user ...");		
		lemonService.changeEmail(changeEmailCode);
	}

}
