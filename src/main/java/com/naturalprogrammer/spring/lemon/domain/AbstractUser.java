package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.security.LemonGrantedAuthority;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
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
public abstract class AbstractUser
	<U extends AbstractUser<U,ID>, ID extends Serializable>
extends VersionedEntity<U, ID>
implements UserDetails {
	
	private static final Log log = LogFactory.getLog(AbstractUser.class); 
			
	private static final long serialVersionUID = 655067760361294864L;
	
	public static final int EMAIL_MIN = 4;
	public static final int EMAIL_MAX = 250;
	
	public static final int UUID_LENGTH = 36;
	
	public static final int PASSWORD_MAX = 50;
	public static final int PASSWORD_MIN = 6;
	
	/**
	 * Role constants. To allow extensibility, this couldn't
	 * be made an enum
	 */
	public static interface Role {

		static final String UNVERIFIED = "UNVERIFIED";
		static final String BLOCKED = "BLOCKED";
		static final String ADMIN = "ADMIN";
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
	private Set<String> roles = new HashSet<String>();
	
	// verification code
	@Column(length = UUID_LENGTH, unique=true)
	protected String verificationCode;
	
	// forgot password code
	@Column(length = UUID_LENGTH, unique=true)
	protected String forgotPasswordCode;
	
	// in the email-change process, temporarily stores the new email
	@UniqueEmail(groups = {ChangeEmailValidation.class})
	@Column(length = EMAIL_MAX)
	protected String newEmail;

	// change email code
	@Column(length = UUID_LENGTH, unique=true)
	protected String changeEmailCode;
	
	// The authentication token
	protected String apiKey;	

	// holds reCAPTCHA response while signing up
	@Transient
	@JsonView(SignupInput.class)
	@Captcha(groups = {SignUpValidation.class})
	private String captchaResponse;
	
	// redundant transient fields
	
	@Transient
	protected boolean unverified = false;

	@Transient
	protected boolean blocked = false;

	@Transient
	protected boolean admin = false;

	@Transient
	protected boolean goodUser = false;

	@Transient
	protected boolean goodAdmin = false;

	@Transient
	protected boolean editable = false;
	
	@Transient
	protected boolean rolesEditable = false;	
	
	// getters and setters
	
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
	
	public String getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}

	public String getChangeEmailCode() {
		return changeEmailCode;
	}

	public void setChangeEmailCode(String changeEmailCode) {
		this.changeEmailCode = changeEmailCode;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
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

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// override this method
	// if email isn't your username
	@Override
	public String getUsername() {
		return email;
	}
	
	// override this method
	// if email isn't your username
	public void setUsername(String username) {
		email = username;
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

	public final boolean hasRole(String role) {
		return roles.contains(role);
	}
	
	public boolean isGoodUser() {
		return goodUser;
	}
	
	public boolean isGoodAdmin() {
		return goodAdmin;
	}

	
	/**
	 * Sets the transient fields of this user
	 * 
	 * @return	this user
	 */
	public U decorate() {
		// delegates
		return decorate(LemonUtils.getUser());
	}
	
	
	/**
	 * Sets the transient fields of this user,
	 * given the current-user
	 * 
	 * @param currentUser	the current-user
	 * @return	this user
	 */
	public U decorate(U currentUser) {
				
		unverified = hasRole(Role.UNVERIFIED);
		blocked = hasRole(Role.BLOCKED);
		admin = hasRole(Role.ADMIN);
		goodUser = !(unverified || blocked);
		goodAdmin = goodUser && admin;
		
		editable = false;
		rolesEditable = false;
		
		if (currentUser != null) {
			editable = currentUser.isGoodAdmin() || equals(currentUser); // admin or self
			rolesEditable = currentUser.isGoodAdmin() && !equals(currentUser); // another admin
		}
		
		computeAuthorities();
		
		log.debug("Decorated user: " + this);

		return (U) this;
	}
	
	
	/**
	 * Hides the confidential fields before sending to client
	 */
	public void hideConfidentialFields() {
		
		setCreatedDate(null);
		setLastModifiedDate(null);
		password = null;
		verificationCode = null;
		forgotPasswordCode = null;
		
		if (!editable)
			email = null;
		
		log.debug("Hid confidential fields for user: " + this);
	}

	
	/**
	 * Called by spring security permission evaluator
	 * to check whether the current-user has the given permission
	 * on this entity. 
	 */
	@Override
	public boolean hasPermission(U currentUser, String permission) {
		
		log.debug("Computing " + permission	+ " permission for : " + this
			+ "\n  Logged in user: " + currentUser);

		// decorate this entity
		decorate(currentUser);
		
		if (permission.equals("edit"))
			return editable;

		return false;
	}

	
	/**
	 * Sets the Id of the user. setId is protected,
	 * hence this had to be coded
	 */
	public void setIdForClient(ID id) {
		setId(id);
	}
	
	
	/**
	 * A convenient toString method
	 */
	@Override
	public String toString() {
		return "AbstractUser [username=" + getUsername() + ", roles=" + roles + "]";
	}
	
	@Transient
	protected Collection<LemonGrantedAuthority> authorities;
	
	/**
	 * Returns the authorities, for Spring Security
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {		
		return authorities;
	}
	
	
	/**
	 * Computes the authorities for Spring Security, and stores
	 * those in the authorities transient field
	 */
	protected void computeAuthorities() {
		
		authorities = roles.stream()
			.map(role -> new LemonGrantedAuthority("ROLE_" + role))
			.collect(Collectors.toCollection(() ->
				new ArrayList<LemonGrantedAuthority>(roles.size() + 2))); 
		
		if (goodUser) {
			
			authorities.add(new LemonGrantedAuthority("ROLE_"
					+ LemonSecurityConfig.GOOD_USER));
			
			if (goodAdmin)
				authorities.add(new LemonGrantedAuthority("ROLE_"
						+ LemonSecurityConfig.GOOD_ADMIN));			
		}

		log.debug("Authorities of " + this + ": " + authorities);
	}

	
	/**
	 * The following are needed
	 * because we have implemented UserDetails. 
	 */
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
