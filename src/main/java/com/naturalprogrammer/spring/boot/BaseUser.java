package com.naturalprogrammer.spring.boot;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.naturalprogrammer.spring.boot.mail.MailSender;
import com.naturalprogrammer.spring.boot.security.UserDto;

@MappedSuperclass
public abstract class BaseUser<U extends BaseUser<U,ID>, ID extends Serializable> extends VersionedEntity<U, ID> {
	
	private static final long serialVersionUID = 655067760361294864L;
	
	public static final int EMAIL_MAX = 250;
	public static final int NAME_MAX = 50;
	public static final String EMAIL_PATTERN = "[A-Za-z0-9._%-+]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	public static final int UUID_LENGTH = 36;
	public static final int PASSWORD_MAX = 30;
	public static final int PASSWORD_MIN = 6;
	public static final int NAME_MIN = 1;
	public static final String ONLY_EMAIL_REGEX = null;
	
	public static enum Role {
		UNVERIFIED, BLOCKED, ADMIN
	}
	
	@Column(nullable = false, length = EMAIL_MAX)
	private String email;
	
	@Column(nullable = false, length = NAME_MAX)
	private String name;
	
	// no length because it will be encrypted
	@Column(nullable = false)
	private String password;
	
	@Column(length = UUID_LENGTH)
	private String verificationCode;
	
	@Column(length = UUID_LENGTH)
	private String forgotPasswordCode;

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

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Role> roles = new HashSet<Role>();

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

	public boolean isAdmin() {
		return roles.contains(Role.ADMIN);
	}

	public UserDto<ID> getUserDto() {
		
		UserDto<ID> userDto = new UserDto<ID>();
		userDto.setId(getId());
		userDto.setName(name);
		userDto.setRoles(roles);
		
		return userDto;
	}

	public static <U extends BaseUser<U,ID>, ID extends Serializable> BaseUser<U, ID> of(SignupForm signupForm) {
		 
		final BaseUser<U,ID> baseUser = SaUtil.getBean(BaseUser.class);
			
		baseUser.setEmail(signupForm.getEmail());
		baseUser.setName(signupForm.getName());
		baseUser.setPassword(signupForm.getPassword());
		baseUser.getRoles().add(Role.UNVERIFIED);
		
		return baseUser;
			
	}
	
//	public boolean isEditable() {
//		BaseUser loggedIn = MyUtil.getSessionUser();
//		if (loggedIn == null)
//			return false;
//		return loggedIn.isAdmin() ||   // ADMIN or
//			   loggedIn.getId() == id; // self can edit
//	}

}
