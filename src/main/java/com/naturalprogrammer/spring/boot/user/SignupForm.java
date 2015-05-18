package com.naturalprogrammer.spring.boot.user;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import com.naturalprogrammer.spring.boot.entities.User;

public abstract class SignupForm {

	@Size(min=User.NAME_MIN, max=User.NAME_MAX)
	private String name;
	
	@NotNull
	@Size(min=1, max=User.EMAIL_MAX)
	@Email
	@UniqueEmail
	private String email;
	
	@Size(min=User.PASSWORD_MIN, max=User.PASSWORD_MAX, message="Inappropriate length")
	private String password;
		
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
	
}
