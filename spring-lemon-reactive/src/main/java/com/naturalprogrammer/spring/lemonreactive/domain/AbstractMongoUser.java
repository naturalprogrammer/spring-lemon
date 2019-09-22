package com.naturalprogrammer.spring.lemonreactive.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.security.core.CredentialsContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.commons.domain.LemonUser;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.commons.validation.Captcha;
import com.naturalprogrammer.spring.lemon.commons.validation.Password;
import com.naturalprogrammer.spring.lemon.commonsmongo.AbstractDocument;
import com.naturalprogrammer.spring.lemonreactive.validation.UniqueEmail;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class AbstractMongoUser
	<ID extends Serializable>
	extends AbstractDocument<ID>
	implements CredentialsContainer, LemonUser<ID> {

	// email
	@JsonView(UserUtils.SignupInput.class)
	@UniqueEmail(groups = {UserUtils.SignUpValidation.class})
	@Indexed(unique = true)
	protected String email;
	
	// password
	@JsonView(UserUtils.SignupInput.class)
	@Password(groups = {UserUtils.SignUpValidation.class, UserUtils.ChangeEmailValidation.class})
	protected String password;
	
	protected Set<String> roles = new HashSet<>();
	
	@Indexed(unique = true, sparse = true)
	protected String newEmail;
	
	@JsonIgnore
	protected long credentialsUpdatedMillis = System.currentTimeMillis();
	
	// holds reCAPTCHA response while signing up
	@Transient
	@JsonView(UserUtils.SignupInput.class)
	@Captcha
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
	public boolean hasPermission(UserDto currentUser, String permission) {
		
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
	public UserDto toUserDto() {
		
		UserDto userDto = new UserDto();
		
		userDto.setId(getId().toString());
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

	@Override
	public void eraseCredentials() {
		password = null;
	}
}
