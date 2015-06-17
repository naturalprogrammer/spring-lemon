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
	public static final int NAME_MAX = 50;
	//public static final String EMAIL_PATTERN = "[A-Za-z0-9._%-+]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	public static final int UUID_LENGTH = 36;
	public static final int PASSWORD_MAX = 30;
	public static final int PASSWORD_MIN = 6;
	public static final int NAME_MIN = 1;
	public static final String ONLY_EMAIL_REGEX = null;
	
	public static interface Role {

		String name();
	
	}
	
	public static enum SaRole implements Role {
		UNVERIFIED, BLOCKED, ADMIN
	}
	
	public interface SignUpValidation {}
	public interface UpdateValidation {}
	
	@Size(min=EMAIL_MIN, max=EMAIL_MAX, groups = {SignUpValidation.class})
	@Email(groups = {SignUpValidation.class})
	@UniqueEmail(groups = {SignUpValidation.class})
	@Column(nullable = false, length = EMAIL_MAX)
	protected String email;
	
	@Size(min=NAME_MIN, max=NAME_MAX, groups = {SignUpValidation.class, UpdateValidation.class})
	@Column(nullable = false, length = NAME_MAX)
	protected String name;
	
	@Password(groups = {SignUpValidation.class})
	@Column(nullable = false) // no length because it will be encrypted
	protected String password;
	
	@Column(length = UUID_LENGTH)
	protected String verificationCode;
	
	@Column(length = UUID_LENGTH)
	protected String forgotPasswordCode;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Role> roles = new HashSet<Role>();

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

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public UserDto<ID> getUserDto() {
		
		UserDto<ID> userDto = new UserDto<ID>();
		userDto.setId(getId());
		userDto.setName(name);
		userDto.setRoles(roles);
		
		return userDto;
	}

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
		
		unverified = roles.contains(SaRole.UNVERIFIED);
		blocked = roles.contains(SaRole.BLOCKED);
		admin = roles.contains(SaRole.ADMIN);
		
		editable = false;
		rolesEditable = false;
		
		if (loggedIn != null) {
			
			boolean adminLoggedIn = loggedIn.getRoles().contains(SaRole.ADMIN);
			
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

}
