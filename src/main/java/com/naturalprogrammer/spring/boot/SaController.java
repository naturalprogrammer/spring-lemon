package com.naturalprogrammer.spring.boot;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class SaController<U extends SaUser, S extends SignupForm> {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private SaService<U, S> saService;
	
	@RequestMapping("/context")
	public ContextDto contextDto() {
		log.info("userDto: " + SaUtil.getUserDto());
		return saService.getContext();
	}
	
	/**
	 * Signup
	 */
	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public void signup(@RequestBody S signupForm,
            		   HttpServletRequest request) {
		
		log.info("Signing up " + signupForm.toString());
		saService.signup(signupForm);

	}


}
