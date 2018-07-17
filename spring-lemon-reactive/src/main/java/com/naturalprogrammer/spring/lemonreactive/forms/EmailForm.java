package com.naturalprogrammer.spring.lemonreactive.forms;

import com.naturalprogrammer.spring.lemon.commons.validation.Password;
import com.naturalprogrammer.spring.lemonreactive.validation.UniqueEmail;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailForm {

	@UniqueEmail
	private String newEmail;
	
	@Password
	private String password;
}
