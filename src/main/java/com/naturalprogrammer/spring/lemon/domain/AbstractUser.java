package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.Captcha;
import com.naturalprogrammer.spring.lemon.validation.Password;
import com.naturalprogrammer.spring.lemon.validation.UniqueEmail;

@MappedSuperclass
public abstract class AbstractUser
	<U extends AbstractUser<U,ID>, ID extends Serializable>
extends VersionedEntity<U, ID>
implements UserDetails {
	
	private static final Log log = LogFactory.getLog(AbstractUser.class); 
			
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
	
//	public static final Set<String> BAD_ROLES = new HashSet<String>(Arrays.asList(
//		new String[] {Role.UNVERIFIED, Role.BLOCKED}
//	));
//	
	public interface SignUpValidation {}
	public interface UpdateValidation {}
	
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

	@Override
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

//	public void setRolesEditable(boolean rolesEditable) {
//		this.rolesEditable = rolesEditable;
//	}
	
	public boolean hasRole(String role) {
		return role.contains(role);
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

//	public static <U extends AbstractUser<U,ID>, ID extends Serializable> AbstractUser<U, ID> of(SignupForm signupForm) {
//		 
//		final AbstractUser<U,ID> baseUser = LemonUtil.getBean(AbstractUser.class);
//			
//		baseUser.setEmail(signupForm.getEmail());
//		baseUser.setName(signupForm.getName());
//		baseUser.setPassword(signupForm.getPassword());
//		baseUser.getRoles().add(Role.UNVERIFIED);
//		
//		return baseUser;
//			
//	}
	
	public boolean isGoodUser() {
		return goodUser;
	}
	
	public boolean isGoodAdmin() {
		return goodUser && admin;
	}
	
//	public boolean decorated() {
//		return unverified || blocked || goodUser;
//	}
//	
	public U decorate() {
		return decorate(LemonUtil.getUser());
	}
	
	public U decorate(U currentUser) {
				
		unverified = roles.contains(Role.UNVERIFIED);
		blocked = roles.contains(Role.BLOCKED);
		admin = roles.contains(Role.ADMIN);
		goodUser = !(unverified || blocked);
		goodAdmin = goodUser && admin;
		
		editable = false;
		rolesEditable = false;
		
		if (currentUser != null) {
			editable = currentUser.isGoodAdmin() || equals(currentUser); // admin or self
			rolesEditable = currentUser.isGoodAdmin() && !equals(currentUser); // another admin
		}
		
		log.debug("Decorated user: " + this);

		return (U) this;

	}
	
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

	@Override
	public boolean hasPermission(U currentUser, String permission) {
		
		log.debug("Computing " + permission	+ " permission for : " + this
			+ "\n  Logged in user: " + currentUser);

		decorate(currentUser);
		
		if (permission.equals("edit"))
			return editable;

		return false;
	}

	public void setIdForClient(ID id) {
		setId(id);
	}
	
	@Override
	public String toString() {
		return "AbstractUser [email=" + email + ", roles=" + roles + "]";
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(
				roles.size() + 2);
	
		for (String role : roles)
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
	
		if (goodUser) {
			
			authorities.add(new SimpleGrantedAuthority("ROLE_" + LemonSecurityConfig.GOOD_USER));
			
			if (goodAdmin)
				authorities.add(new SimpleGrantedAuthority("ROLE_" + LemonSecurityConfig.GOOD_ADMIN));			
		}

		log.debug("Authorities of " + this + ": " + authorities);

		return authorities;
		
	}

	@Override
	public String getUsername() {
		return email;
	}

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
