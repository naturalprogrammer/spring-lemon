package com.naturalprogrammer.spring.boot.user;
//package com.bridgetonresearch.sps.core.signup;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.validation.Errors;
//
//import com.bridgetonresearch.sps.core.common.UserService;
//import com.bridgetonresearch.sps.util.validation.Validator;
//
//@Component
//public class SignupFormValidator implements Validator<SignupForm> {
//
//	@Autowired
//	private UserService userService;
//	
//	@Override
//	public void validate(SignupForm signupForm, Errors errors) {
//		
//		  if (userService.findByEmail(signupForm.getEmail()) != null)
//			  errors.rejectValue(null, "unique.email", "Email id already exists");
//
//	}
//
//}
