package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.commons.validation.Captcha;
import com.naturalprogrammer.spring.lemon.commons.validation.Password;
import com.naturalprogrammer.spring.lemon.validation.UniqueEmail;

import lombok.Getter;
import lombok.Setter;


/**
 * Base class for User entity
 * 
 * @author Sanjay Patel
 */
@Getter @Setter
@MappedSuperclass
public class AbstractUser
	<U extends AbstractUser<U,ID>,
	 ID extends Serializable>
extends VersionedEntity<U, ID> {
	
	// email
	@JsonView(UserUtils.SignupInput.class)
	@UniqueEmail(groups = {UserUtils.SignUpValidation.class})
	@Column(nullable = false, unique=true, length = UserUtils.EMAIL_MAX)
	protected String email;
	
	// password
	@JsonView(UserUtils.SignupInput.class)
	@Password(groups = {UserUtils.SignUpValidation.class, UserUtils.ChangeEmailValidation.class})
	@Column(nullable = false) // no length because it will be encrypted
	protected String password;
	
	// roles collection
	@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="usr_role", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="role")
	protected Set<String> roles = new HashSet<>();
	
	// in the email-change process, temporarily stores the new email
	@UniqueEmail(groups = {UserUtils.ChangeEmailValidation.class})
	@Column(length = UserUtils.EMAIL_MAX)
	protected String newEmail;

	// A JWT issued before this won't be valid
	@Column(nullable = false)
	@JsonIgnore
	protected long credentialsUpdatedMillis = System.currentTimeMillis();

	// holds reCAPTCHA response while signing up
	@Transient
	@JsonView(UserUtils.SignupInput.class)
	@Captcha(groups = {UserUtils.SignUpValidation.class})
	private String captchaResponse;
	
	public final boolean hasRole(String role) {
		return roles.contains(role);
	}	
	
	/**
	 * Called by spring security permission evaluator
	 * to check whether the current-user has the given permission
	 * on this entity. 
	 */
	@Override
	public boolean hasPermission(UserDto<?> currentUser, String permission) {
		
		return UserUtils.hasPermission(getId(), currentUser, permission);		
	}

	
	/**
	 * A convenient toString method
	 */
	@Override
	public String toString() {
		return "AbstractUser [email=" + email + ", roles=" + roles + "]";
	}


	/**
	 * Makes a User DTO
	 */
	public UserDto<ID> toUserDto() {
		
		UserDto<ID> userDto = new UserDto<>();
		
		userDto.setId(getId());
		userDto.setUsername(email);
		userDto.setPassword(password);
		userDto.setRoles(roles);
		userDto.setTag(toTag());
		
		userDto.initialize();

		return userDto;
	}

	/**
	 * Override this to supply any additional fields to the user DTO,
	 * e.g. name
	 */
	protected Serializable toTag() {
		
		return null;
	}
}
