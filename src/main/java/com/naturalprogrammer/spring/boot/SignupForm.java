//package com.naturalprogrammer.spring.boot;
//
//import javax.validation.constraints.Size;
//
//import org.hibernate.validator.constraints.Email;
//import org.hibernate.validator.constraints.NotBlank;
//
//import com.naturalprogrammer.spring.boot.domain.AbstractUser;
//import com.naturalprogrammer.spring.boot.validation.Captcha;
//import com.naturalprogrammer.spring.boot.validation.Password;
//import com.naturalprogrammer.spring.boot.validation.UniqueEmail;
//
///**
// * See http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-builtin-constraints
// * 
// * @author skpat_000
// *
// */
//public class SignupForm {
//
//	@Size(min=AbstractUser.NAME_MIN, max=AbstractUser.NAME_MAX)
//	private String name;
//	
//	@Size(min=4, max=AbstractUser.EMAIL_MAX)
//	@Email
//	@UniqueEmail
//	private String email;
//	
//	@Password
//	private String password;
//	
//	@Captcha
//	private String captchaResponse;
//		
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}
//
//	public String getPassword() {
//		return password;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
//	@Override
//	public String toString() {
//		return "SignupForm [name=" + name + ", email=" + email + "]";
//	}
//
//	public String getCaptchaResponse() {
//		return captchaResponse;
//	}
//
//	public void setCaptchaResponse(String captchaResponse) {
//		this.captchaResponse = captchaResponse;
//	}
//	
//}
