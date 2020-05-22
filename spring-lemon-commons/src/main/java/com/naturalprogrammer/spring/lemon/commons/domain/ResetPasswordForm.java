package com.naturalprogrammer.spring.lemon.commons.domain;

import com.naturalprogrammer.spring.lemon.commons.validation.Password;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class ResetPasswordForm {
	
	@NotBlank
	private String code;
	
	@Password
	private String newPassword;
}
