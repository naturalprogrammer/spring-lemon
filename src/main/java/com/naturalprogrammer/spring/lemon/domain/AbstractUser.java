package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
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
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.security.LemonGrantedAuthority;
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
	
	// One time token
	private String nonce;
	
	// A JWT after before this won't be valid
	@Column(nullable = false)
	private Date credentialsUpdatedAt = new Date();

	// holds reCAPTCHA response while signing up
	@Transient
	@JsonView(SignupInput.class)
	@Captcha(groups = {SignUpValidation.class})
	private String captchaResponse;
	
	// redundant transient fields
	
//	@Transient
//	protected boolean unverified = false;
//
//	@Transient
//	protected boolean blocked = false;
//
//	@Transient
//	protected boolean admin = false;
//
//	@Transient
//	protected boolean goodUser = false;
//
//	@Transient
//	protected boolean goodAdmin = false;
//
//	@Transient
//	protected boolean editable = false;
//	
//	@Transient
//	protected boolean rolesEditable = false;	
	
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
	
	
//	/**
//	 * Sets the transient fields of this user
//	 * 
//	 * @return	this user
//	 */
//	public U decorate() {
//		// delegates
//		return decorate(LemonUtils.getUser());
//	}
	
	
//	/**
//	 * Sets the transient fields of this user,
//	 * given the current-user
//	 * 
//	 * @param currentUser	the current-user
//	 * @return	this user
//	 */
//	public U decorate(LemonPrincipal<?> currentUser) {
//				
//		unverified = hasRole(Role.UNVERIFIED);
//		blocked = hasRole(Role.BLOCKED);
//		admin = hasRole(Role.ADMIN);
//		goodUser = !(unverified || blocked);
//		goodAdmin = goodUser && admin;
//		
//		editable = false;
//		rolesEditable = false;
//		
//		if (currentUser != null) {
//			
//			boolean self = currentUser.getUserId().equals(getId());
//			
//			editable = self || currentUser.isGoodAdmin(); // self or admin
//			rolesEditable = currentUser.isGoodAdmin() && !self; // another admin
//		}
//		
//		computeAuthorities();
//		
//		log.debug("Decorated user: " + this);
//
//		return (U) this;
//	}
	
	
	/**
	 * Hides the confidential fields before sending to client
	 */
	public void hideConfidentialFields() {
		
		//setCreatedDate(null);
		//setLastModifiedDate(null);
		password = null;
		verificationCode = null;
		forgotPasswordCode = null;
		
		if (!hasPermission(LemonUtils.getSpringUser(), Permission.EDIT))
			email = null;
		
		log.debug("Hid confidential fields for user: " + this);
	}

	
	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public Date getCredentialsUpdatedAt() {
		return credentialsUpdatedAt;
	}

	public void setCredentialsUpdatedAt(Date credentialsUpdatedAt) {
		this.credentialsUpdatedAt = credentialsUpdatedAt;
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
		return "AbstractUser [email=" + email + ", roles=" + roles + "]";
	}
	
	@Transient
	protected Collection<LemonGrantedAuthority> authorities;
	
	/**
	 * Returns the authorities, for Spring Security
	 */
	public Collection<? extends GrantedAuthority> getAuthorities() {		
		return authorities;
	}
	
	
//	/**
//	 * Computes the authorities for Spring Security, and stores
//	 * those in the authorities transient field
//	 * @return 
//	 */
//	protected void computeAuthorities() {
//		
//		authorities = roles.stream()
//			.map(role -> new LemonGrantedAuthority("ROLE_" + role))
//			.collect(Collectors.toCollection(() ->
//				new ArrayList<LemonGrantedAuthority>(roles.size() + 2))); 
//		
//		if (goodUser) {
//			
//			authorities.add(new LemonGrantedAuthority("ROLE_"
//					+ LemonSecurityConfig.GOOD_USER));
//			
//			if (goodAdmin)
//				authorities.add(new LemonGrantedAuthority("ROLE_"
//						+ LemonSecurityConfig.GOOD_ADMIN));
//		}
//
//		log.debug("Authorities of " + this + ": " + authorities);
//		
//		return authorities;
//	}
	
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
