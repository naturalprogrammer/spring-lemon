package com.naturalprogrammer.spring.boot;

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

import com.naturalprogrammer.spring.boot.domain.BaseUser;
import com.naturalprogrammer.spring.boot.util.SaUtil;

public class SaController<U extends BaseUser<U,ID>, ID extends Serializable> {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private SaService<U, ID> saService;
	
	@RequestMapping("/ping")
	public void ping() {
		log.info("Received a ping");
	}
	
	@RequestMapping("/context")
	public Map<String, Object> contextDto() {
		return SaUtil.mapOf("context", saService.getContext(),
							"loggedIn", saService.userForClient());
	}
	
	/**
	 * Signup
	 */
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public U signup(@RequestBody U user) {
		
		return saService.signup(user);

	}

	
	/**
	 * Verify
	 */
	@RequestMapping(value="/users/{verificationCode}/verify", method=RequestMethod.POST)
	public void verifyUser(@PathVariable("verificationCode") String verificationCode) {
		
		saService.verifyUser(verificationCode);

	}


	/**
	 * Forgot Password
	 */
	@RequestMapping(value="/forgot-password", method=RequestMethod.POST)
	public void forgotPassword(@RequestParam("email") String email) {
		
		saService.forgotPassword(email);

	}
	
	@RequestMapping(value="/users/fetch-by-email")
	public U fetchByEmail(@RequestParam("email") String email) {
		
		return saService.fetchUser(email);

	}
	
	@RequestMapping(value="/users/{id}/fetch-by-id")
	public U fetchById(@PathVariable("id") U user) {
		
		return saService.fetchUser(user);

	}

	
	/**
	 * Reset Password
	 */
	@RequestMapping(value="/users/{forgotPasswordCode}/reset-password", method=RequestMethod.POST)
	public void resetPassword(@PathVariable("forgotPasswordCode") String forgotPasswordCode, @RequestParam("newPassword") String newPassword) {
		
		saService.resetPassword(forgotPasswordCode, newPassword);

	}


	/**
	 * Update
	 */
	@RequestMapping(value="/users/{id}/update", method=RequestMethod.POST)
	public U signup(@PathVariable("id") U user, @RequestBody U updatedUser) {
		
		return saService.updateUser(user, updatedUser);

	}


}
