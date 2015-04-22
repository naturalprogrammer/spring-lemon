package com.naturalprogrammer.spring.boot.user;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.boot.Sa;
import com.naturalprogrammer.spring.boot.user.BaseUser.Role;

@Service
@Validated
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class SignupService {

    @Autowired
    private UserService userService;

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid SignupForm signupForm) {
		
		final BaseUser baseUser = Sa.getBean(BaseUser.class);
		
		baseUser.setEmail(signupForm.getEmail());
		baseUser.setName(signupForm.getName());
		baseUser.setPassword(signupForm.getPassword());
		baseUser.getRoles().add(Role.UNVERIFIED);
		userService.save(baseUser);
		
	}

}
