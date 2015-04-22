package com.naturalprogrammer.spring.boot.user;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignupController {
	
	private static final Logger logger = LoggerFactory.getLogger(SignupController.class);
	
	private SignupService signupService;
	
	@Autowired
	public SignupController(SignupService signupService) {
		this.signupService = signupService;
	}
	
	/**
	 * Signup
	 */
	@RequestMapping(value="/api/core/signup", method=RequestMethod.POST)
	public void signup(@RequestBody SignupForm joinData,
            		   HttpServletRequest request) {
		
		logger.info("signing up " + joinData.toString());
		signupService.signup(joinData);

	}


}
