package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

public class SaController<U extends AbstractUser<U,ID>, ID extends Serializable> {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private LemonService<U, ID> lemonService;
	
	@RequestMapping(value="/ping", method=RequestMethod.GET)
	public void ping() {
		log.info("Received a ping");
	}
	
	@RequestMapping(value="/context", method=RequestMethod.GET)
	public Map<String, Object> context() {
		return LemonUtil.mapOf("context", lemonService.getContext(),
							"loggedIn", lemonService.userForClient());
	}
	
	/**
	 * Signup
	 */
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public U signup(@RequestBody U user) {
		
		lemonService.signup(user);
		return lemonService.userForClient();

	}

	
	/**
	 * Verify
	 */
	@RequestMapping(value="/users/{verificationCode}/verify", method=RequestMethod.POST)
	public void verifyUser(@PathVariable("verificationCode") String verificationCode) {
		
		lemonService.verifyUser(verificationCode);

	}


	/**
	 * Forgot Password
	 */
	@RequestMapping(value="/forgot-password", method=RequestMethod.POST)
	public void forgotPassword(@RequestParam("email") String email) {
		
		lemonService.forgotPassword(email);

	}
	
	@RequestMapping(value="/users/fetch-by-email", method=RequestMethod.GET)
	public U fetchByEmail(@RequestParam("email") String email) {
		
		return lemonService.fetchUser(email);

	}
	
	@RequestMapping(value="/users/{id}/fetch-by-id", method=RequestMethod.GET)
	public U fetchById(@PathVariable("id") U user) {
		
		return lemonService.fetchUser(user);

	}

	
	/**
	 * Reset Password
	 */
	@RequestMapping(value="/users/{forgotPasswordCode}/reset-password", method=RequestMethod.POST)
	public void resetPassword(@PathVariable("forgotPasswordCode") String forgotPasswordCode, @RequestParam("newPassword") String newPassword) {
		
		lemonService.resetPassword(forgotPasswordCode, newPassword);

	}


	/**
	 * Update
	 */
	@RequestMapping(value="/users/{id}/update", method=RequestMethod.PATCH)
	public U updateUser(@PathVariable("id") U user, @RequestBody U updatedUser) {
		
		lemonService.updateUser(user, updatedUser);
		return lemonService.userForClient();
		
	}
	
	
	/**
	 * Change Password
	 */
	@RequestMapping(value="/users/{id}/change-password", method=RequestMethod.PATCH)
	public void changePassword(@PathVariable("id") U user, @RequestBody ChangePasswordForm changePasswordForm) {
		
		lemonService.changePassword(user, changePasswordForm);

	}



}
