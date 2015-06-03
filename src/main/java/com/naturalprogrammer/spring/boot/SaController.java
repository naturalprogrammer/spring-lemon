package com.naturalprogrammer.spring.boot;

import java.io.Serializable;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.naturalprogrammer.spring.boot.security.UserDto;

public class SaController<U extends BaseUser<U,ID>, ID extends Serializable, S extends SignupForm> {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private SaService<U, ID, S> saService;
	
	@RequestMapping("/ping")
	public void ping() {
		log.info("Received a ping");
	}
	
	@RequestMapping("/context")
	public ContextDto contextDto() {
		log.info("userDto: " + SaUtil.getUserDto());
		return saService.getContext();
	}
	
	/**
	 * Signup
	 */
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public UserDto<ID> signup(@RequestBody S signupForm) {
		
		return saService.signup(signupForm);

	}

	
	/**
	 * Verify
	 */
	@RequestMapping(value="/users/{verificationCode}/verify", method=RequestMethod.POST)
	public void verifyUser(@PathVariable("verificationCode") String verificationCode) {
		
		saService.verifyUser(verificationCode);

	}

	
	@RequestMapping(value="/users/fetch-by-email")
	public U fetchByEmail(@RequestParam("email") String email,
            		   HttpServletRequest request) {
		
		return saService.fetchUser(email);

	}



}
