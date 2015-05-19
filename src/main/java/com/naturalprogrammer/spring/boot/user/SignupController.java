//package com.naturalprogrammer.spring.boot.user;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.naturalprogrammer.spring.boot.SaUser;
//import com.naturalprogrammer.spring.boot.SignupForm;
//
//public abstract class SignupController<U extends SaUser, S extends SignupForm> {
//	
//	private static final Logger logger = LoggerFactory.getLogger(SignupController.class);
//	
//	@Autowired
//	private SignupService<U> signupService;
//	
//	/**
//	 * Signup
//	 */
//	@RequestMapping(value="/api/core/signup", method=RequestMethod.POST)
//	public void signup(@RequestBody S joinData,
//            		   HttpServletRequest request) {
//		
//		logger.info("signing up " + joinData.toString());
//		signupService.signup(joinData);
//
//	}
//
//}
