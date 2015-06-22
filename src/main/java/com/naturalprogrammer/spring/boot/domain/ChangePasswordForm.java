package com.naturalprogrammer.spring.boot.domain;

import com.naturalprogrammer.spring.boot.validation.Password;
import com.naturalprogrammer.spring.boot.validation.RetypePassword;
import com.naturalprogrammer.spring.boot.validation.RetypePasswordForm;

@RetypePassword
public class ChangePasswordForm implements RetypePasswordForm {

	@Password
	private String oldPassword;

	@Password
	private String password;
	
	@Password
	private String retypePassword;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getRetypePassword() {
		return retypePassword;
	}

	public void setRetypePassword(String retypePassword) {
		this.retypePassword = retypePassword;
	}
	
}
