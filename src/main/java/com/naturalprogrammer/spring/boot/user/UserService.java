package com.naturalprogrammer.spring.boot.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	
	@Autowired
	private BaseUserRepository userRepository;

	public BaseUser findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public BaseUser findByForgotPasswordCode(String forgotPasswordCode) {
		return userRepository.findByForgotPasswordCode(forgotPasswordCode);
	}

	public void save(BaseUser baseUser) {
		userRepository.save(baseUser);
	}
	
}
