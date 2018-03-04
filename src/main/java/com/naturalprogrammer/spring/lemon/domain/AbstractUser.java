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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.security.SpringUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.Captcha;
import com.naturalprogrammer.spring.lemon.validation.Password;
import com.naturalprogrammer.spring.lemon.validation.UniqueEmail;


/**
 * Base class for User entity
 * 
 * @author Sanjay Patel
 *
 * @param <U>	The User class
 * @param <ID>	The Primary key type of User class 
 */
@MappedSuperclass
public class AbstractUser
	<U extends AbstractUser<U,ID>,
	 ID extends Serializable>
extends VersionedEntity<U, ID> {
	
	private static final Log log = LogFactory.getLog(AbstractUser.class); 
			
	public static final int EMAIL_MIN = 4;
	public static final int EMAIL_MAX = 250;
	
	public static final int UUID_LENGTH = 36;
	
	public static final int PASSWORD_MAX = 50;
	public static final int PASSWORD_MIN = 6;
	
	/**
	 * Role constants. To allow extensibility, this couldn't
	 * be made an enum
	 */
	public interface Role {

		static final String UNVERIFIED = "UNVERIFIED";
		static final String BLOCKED = "BLOCKED";
		static final String ADMIN = "ADMIN";
	}
	
	public interface Permission {
		
		static final String EDIT = "edit";		
	}
	
	// validation groups
	public interface SignUpValidation {}
	public interface UpdateValidation {}
	public interface ChangeEmailValidation {}
	
	// JsonView for Sign up
	public interface SignupInput {}
	
	// email
	@JsonView(SignupInput.class)
	@UniqueEmail(groups = {SignUpValidation.class})
	@Column(nullable = false, unique=true, length = EMAIL_MAX)
	protected String email;
	
	// password
	@JsonView(SignupInput.class)
	@Password(groups = {SignUpValidation.class, ChangeEmailValidation.class})
	@Column(nullable = false) // no length because it will be encrypted
	protected String password;
	
	// roles collection
	@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="usr_role", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="role")
	private Set<String> roles = new HashSet<>();
	
//	// verification code
//	@Column(length = UUID_LENGTH, unique=true)
//	protected String verificationCode;
	
//	// forgot password code
//	@Column(length = UUID_LENGTH, unique=true)
//	protected String forgotPasswordCode;
//	
	// in the email-change process, temporarily stores the new email
	@UniqueEmail(groups = {ChangeEmailValidation.class})
	@Column(length = EMAIL_MAX)
	protected String newEmail;

//	// change email code
//	@Column(length = UUID_LENGTH, unique=true)
//	protected String changeEmailCode;
	
	// One time token
//	@JsonIgnore
//	private String nonce;
	
	// A JWT after before this won't be valid
	@Column(nullable = false)
	@JsonIgnore
	private long credentialsUpdatedMillis = System.currentTimeMillis();

	// holds reCAPTCHA response while signing up
	@Transient
	@JsonView(SignupInput.class)
	@Captcha(groups = {SignUpValidation.class})
	private String captchaResponse;
	
	// getters and setters
	
//	public String getVerificationCode() {
//		return verificationCode;
//	}
//
//	public void setVerificationCode(String verificationCode) {
//		this.verificationCode = verificationCode;
//	}

//	public String getForgotPasswordCode() {
//		return forgotPasswordCode;
//	}
//
//	public void setForgotPasswordCode(String forgotPasswordCode) {
//		this.forgotPasswordCode = forgotPasswordCode;
//	}
//	
	public String getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}

//	public String getChangeEmailCode() {
//		return changeEmailCode;
//	}
//
//	public void setChangeEmailCode(String changeEmailCode) {
//		this.changeEmailCode = changeEmailCode;
//	}
//
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

	public final boolean hasRole(String role) {
		return roles.contains(role);
	}
	
	
	/**
	 * Hides the confidential fields before sending to client
	 */
	public void hideConfidentialFields() {
		
		password = null; // JsonIgnore didn't work because of JsonIgnore
		//verificationCode = null;
		//forgotPasswordCode = null;
		
		if (!hasPermission(LemonUtils.getSpringUser(), Permission.EDIT))
			email = null;
		
		log.debug("Hid confidential fields for user: " + this);
	}
	
//	public String getNonce() {
//		return nonce;
//	}
//
//	public void setNonce(String nonce) {
//		this.nonce = nonce;
//	}
//
	public long getCredentialsUpdatedMillis() {
		return credentialsUpdatedMillis;
	}

	public void setCredentialsUpdatedMillis(long credentialsUpdatedMillis) {
		this.credentialsUpdatedMillis = credentialsUpdatedMillis;
	}

	/**
	 * Called by spring security permission evaluator
	 * to check whether the current-user has the given permission
	 * on this entity. 
	 */
	@Override
	public boolean hasPermission(SpringUser<?> currentUser, String permission) {
		
		log.debug("Computing " + permission	+ " permission for : " + this
			+ "\n  Logged in user: " + currentUser);


		if (permission.equals("edit")) {
			
			if (currentUser == null)
				return false;
			
			boolean self = currentUser.getId().equals(getId());		
			return self || currentUser.isGoodAdmin(); // self or admin;			
		}

		return false;
	}

	
	/**
	 * A convenient toString method
	 */
	@Override
	public String toString() {
		return "AbstractUser [email=" + email + ", roles=" + roles + "]";
	}


	/**
	 * Makes a Spring User
	 * 
	 * @return
	 */
	public SpringUser<ID> toSpringUser() {
		
		SpringUser<ID> springUser = new SpringUser<>();
		
		springUser.setId(getId());
		springUser.setUsername(email);
		springUser.setPassword(password);
		springUser.setRoles(roles);
		springUser.setTag(toTag());
		
		boolean unverified = hasRole(Role.UNVERIFIED);
		boolean blocked = hasRole(Role.BLOCKED);
		boolean admin = hasRole(Role.ADMIN);
		boolean goodUser = !(unverified || blocked);
		boolean goodAdmin = goodUser && admin;

		springUser.setAdmin(admin);
		springUser.setBlocked(blocked);
		springUser.setGoodAdmin(goodAdmin);
		springUser.setGoodUser(goodUser);
		springUser.setUnverified(unverified);
		
		return springUser;
	}

	protected Serializable toTag() {
		
		return null;
	}
}
