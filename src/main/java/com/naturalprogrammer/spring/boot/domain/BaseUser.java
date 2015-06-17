package com.naturalprogrammer.spring.boot.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import com.naturalprogrammer.spring.boot.util.SaUtil;
import com.naturalprogrammer.spring.boot.validation.Captcha;
import com.naturalprogrammer.spring.boot.validation.Password;
import com.naturalprogrammer.spring.boot.validation.UniqueEmail;

@MappedSuperclass
public abstract class BaseUser<U extends BaseUser<U,ID>, ID extends Serializable> extends VersionedEntity<U, ID> {
	
	private static final long serialVersionUID = 655067760361294864L;
	
	public static final int EMAIL_MIN = 4;
	public static final int EMAIL_MAX = 250;
	//public static final String EMAIL_PATTERN = "[A-Za-z0-9._%-+]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	public static final int UUID_LENGTH = 36;
	public static final int PASSWORD_MAX = 30;
	public static final int PASSWORD_MIN = 6;
	public static final String ONLY_EMAIL_REGEX = null;
	
	public static interface Role {

		static final String UNVERIFIED = "UNVERIFIED";
		static final String BLOCKED = "BLOCKED";
		static final String ADMIN = "ADMIN";
	
	}
	
	public interface SignUpValidation {}
	public interface UpdateValidation {}
	
	@Size(min=EMAIL_MIN, max=EMAIL_MAX, groups = {SignUpValidation.class})
	@Email(groups = {SignUpValidation.class})
	@UniqueEmail(groups = {SignUpValidation.class})
	@Column(nullable = false, length = EMAIL_MAX)
	protected String email;
	
	@Password(groups = {SignUpValidation.class})
	@Column(nullable = false) // no length because it will be encrypted
	protected String password;
	
	@Column(length = UUID_LENGTH)
	protected String verificationCode;
	
	@Column(length = UUID_LENGTH)
	protected String forgotPasswordCode;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> roles = new HashSet<String>();
	
	@Transient
	@Captcha(groups = {SignUpValidation.class})
	private String captchaResponse;
	
	@Transient
	protected boolean unverified = true;

	@Transient
	protected boolean blocked = false;

	@Transient
	protected boolean admin = false;

	@Transient
	protected boolean editable = false;
	
	@Transient
	protected boolean rolesEditable = false;	
	
	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String getForgotPasswordCode() {
		return forgotPasswordCode;
	}

	public void setForgotPasswordCode(String forgotPasswordCode) {
		this.forgotPasswordCode = forgotPasswordCode;
	}
	
	public String getCaptchaResponse() {
		return captchaResponse;
	}

	public void setCaptchaResponse(String captchaResponse) {
		this.captchaResponse = captchaResponse;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
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

	public boolean isUnverified() {
		return unverified;
	}

	public void setUnverified(boolean unverified) {
		this.unverified = unverified;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean isRolesEditable() {
		return rolesEditable;
	}

	public void setRolesEditable(boolean rolesEditable) {
		this.rolesEditable = rolesEditable;
	}

//	public UserDto<ID> getUserDto() {
//		
//		UserDto<ID> userDto = new UserDto<ID>();
//		userDto.setId(getId());
//		userDto.setName(name);
//		userDto.setRoles(roles);
//		
//		return userDto;
//	}

//	public static <U extends BaseUser<U,ID>, ID extends Serializable> BaseUser<U, ID> of(SignupForm signupForm) {
//		 
//		final BaseUser<U,ID> baseUser = SaUtil.getBean(BaseUser.class);
//			
//		baseUser.setEmail(signupForm.getEmail());
//		baseUser.setName(signupForm.getName());
//		baseUser.setPassword(signupForm.getPassword());
//		baseUser.getRoles().add(Role.UNVERIFIED);
//		
//		return baseUser;
//			
//	}
	
	public U decorate() {
		return decorate(SaUtil.getLoggedInUser());
	}
	
	public U decorate(U loggedIn) {
		
		unverified = roles.contains(Role.UNVERIFIED);
		blocked = roles.contains(Role.BLOCKED);
		admin = roles.contains(Role.ADMIN);
		
		editable = false;
		rolesEditable = false;
		
		if (loggedIn != null) {
			
			boolean adminLoggedIn = loggedIn.getRoles().contains(Role.ADMIN);
			
			editable = adminLoggedIn || equals(loggedIn); // admin or self
			rolesEditable = adminLoggedIn && !equals(loggedIn); // another admin
		}
		
		return (U) this;
		
	}

	public void hideConfidentialFields() {
		password = null;
		if (!editable)
			email = "Confidential";
	}


	public boolean hasPermission(U loggedInUser, String permission) {
		
		decorate(loggedInUser);
		
		if (permission.equals("edit"))
			return editable;

		return false;
	}

	public void setIdForClient(ID id) {
		setId(id);
	}
	
	@Override
	public String toString() {
		return "BaseUser [email=" + email + ", verificationCode="
				+ verificationCode + ", forgotPasswordCode="
				+ forgotPasswordCode + ", roles=" + roles + "]";
	}

}
