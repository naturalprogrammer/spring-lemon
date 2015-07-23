package com.naturalprogrammer.spring.lemon.domain;

import com.naturalprogrammer.spring.lemon.validation.Password;
import com.naturalprogrammer.spring.lemon.validation.RetypePassword;
import com.naturalprogrammer.spring.lemon.validation.RetypePasswordForm;

@RetypePassword
public class ChangePasswordForm implements RetypePasswordForm {
	
	public ChangePasswordForm() {}
	
	public ChangePasswordForm(String oldPassword, String password, String retypePassword) {
		this.oldPassword = oldPassword;
		this.password = password;
		this.retypePassword = retypePassword;
	}

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
