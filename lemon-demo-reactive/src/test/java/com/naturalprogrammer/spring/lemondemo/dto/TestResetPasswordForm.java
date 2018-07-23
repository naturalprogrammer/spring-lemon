package com.naturalprogrammer.spring.lemondemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestResetPasswordForm {
	
	private String code;
	private String newPassword;
}
