//package com.naturalprogrammer.spring.boot.user;
//
//import javax.validation.Valid;
//
//import org.apache.commons.lang3.RandomStringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.validation.annotation.Validated;
//
//import com.naturalprogrammer.spring.boot.SaUser;
//import com.naturalprogrammer.spring.boot.SaUtil;
//import com.naturalprogrammer.spring.boot.SaUser.Role;
//
//@Service
//@Validated
//@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
//public class SignupService<U extends SaUser> {
//
//    private final Log log = LogFactory.getLog(getClass());
//
//	@Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private UserService<U> userService;
//    
//	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
//	public void signup(@Valid SignupForm signupForm) {
//		
//		U user = createUser(signupForm);
//		userService.save(user);
//		userService.sendVerificationMail(user);
//		
//	}
//	
//	public U createUser(SignupForm signupForm) {
//		
//		final U user = (U) SaUtil.getBean(SaUser.class);
//		
//		user.setEmail(signupForm.getEmail());
//		user.setName(signupForm.getName());
//		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
//		user.getRoles().add(Role.UNVERIFIED);
//		user.setVerificationCode(RandomStringUtils.randomAlphanumeric(16));
//		
//		return user;
//		
//	}
//	
//	
//
//}
