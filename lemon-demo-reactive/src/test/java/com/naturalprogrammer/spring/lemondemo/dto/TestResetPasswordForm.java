package com.naturalprogrammer.spring.lemondemo.dto;

import javax.validation.constraints.NotBlank;

import com.naturalprogrammer.spring.lemon.commons.validation.Password;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestResetPasswordForm {
	
	private String code;
	private String newPassword;
}
