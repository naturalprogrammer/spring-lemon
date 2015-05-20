package com.naturalprogrammer.spring.boot;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import com.naturalprogrammer.spring.boot.validation.Captcha;
import com.naturalprogrammer.spring.boot.validation.UniqueEmail;

public class SignupForm {

	@Size(min=SaUser.NAME_MIN, max=SaUser.NAME_MAX)
	private String name;
	
	@NotNull
	@Size(min=1, max=SaUser.EMAIL_MAX)
	@Email
	@UniqueEmail
	private String email;
	
	@Size(min=SaUser.PASSWORD_MIN, max=SaUser.PASSWORD_MAX, message="Inappropriate length")
	private String password;
	
	@Captcha
	private String captchaResponse;
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "SignupForm [name=" + name + ", email=" + email + "]";
	}

	public String getCaptchaResponse() {
		return captchaResponse;
	}

	public void setCaptchaResponse(String captchaResponse) {
		this.captchaResponse = captchaResponse;
	}
	
}
