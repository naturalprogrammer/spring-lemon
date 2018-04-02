package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.lemon.LemonProperties.Admin;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.SignUpValidation;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.mail.LemonMailData;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.permissions.UserEditPermission;
import com.naturalprogrammer.spring.lemon.security.JwtService;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.security.UserDto;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.Password;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * The Lemon Service class
 * 
 * @author Sanjay Patel
 *
 * @param <U>	The User class
 * @param <ID>	The Primary key type of User class 
 */
@Validated
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public abstract class LemonService
	<U extends AbstractUser<U,ID>, ID extends Serializable> {

    private static final Log log = LogFactory.getLog(LemonService.class);
    
	private LemonProperties properties;
	private PasswordEncoder passwordEncoder;
    private MailSender mailSender;
	private AbstractUserRepository<U, ID> userRepository;
	private UserDetailsService userDetailsService;
	private JwtService jwtService;

	@Autowired
	public void createLemonService(LemonProperties properties,
			PasswordEncoder passwordEncoder,
			MailSender<?> mailSender,
			AbstractUserRepository<U, ID> userRepository,
			UserDetailsService userDetailsService,
			JwtService jwtService) {
		
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
		this.userRepository = userRepository;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
		
		log.info("Created");
	}

	
	abstract public ID parseId(String id);

	
	/**
     * This method is called after the application is ready.
     * Needs to be public - otherwise Spring screams.
     * 
     * @param event
     */
    @EventListener
    public void afterApplicationReady(ApplicationReadyEvent event) {
    	
    	log.info("Starting up Spring Lemon ...");
    	onStartup(); // delegate to onStartup()
    	log.info("Spring Lemon started");	
    }

    
	/**
	 * Creates the initial Admin user, if not found.
	 * Override this method if needed.
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public void onStartup() {
    	
		try {
			
			// Check if the user already exists
			userDetailsService
				.loadUserByUsername(properties.getAdmin().getUsername());
			
		} catch (UsernameNotFoundException e) {
			
			// Doesn't exist. So, create it.
	    	U user = createAdminUser();
	    	userRepository.save(user);			
		}
	}


	/**
	 * Creates the initial Admin user.
	 * Override this if needed.
	 */
    protected U createAdminUser() {
		
    	// fetch data about the user to be created
    	Admin initialAdmin = properties.getAdmin();
    	
    	log.info("Creating the first admin user: " + initialAdmin.getUsername());

    	// create the user
    	U user = newUser();
    	user.setEmail(initialAdmin.getUsername());
		user.setPassword(passwordEncoder.encode(
			properties.getAdmin().getPassword()));
		user.getRoles().add(Role.ADMIN);
		
		return user;
	}

    
	/**
	 * Creates a new user object. Must be overridden in the
	 * subclass, like this:
	 * 
	 * <pre>
	 * protected User newUser() {
	 *    return new User();
	 * }
	 * </pre>
	 */
    public abstract U newUser();


	/**
	 * Returns the context data to be sent to the client,
	 * i.e. <code>reCaptchaSiteKey</code> and all the properties
	 * prefixed with <code>lemon.shared</code>.
	 * 
	 * To send custom properties, put those in your application
	 * properties in the format <em>lemon.shared.fooBar</em>.
	 * 
	 * Override this method if needed.
	 * @param response 
	 * @param expirationMillis 
	 */
	public Map<String, Object> getContext(Optional<Long> expirationMillis, HttpServletResponse response) {
		
		log.debug("Getting context ...");

		// make the context
		Map<String, Object> sharedProperties = new HashMap<String, Object>(2);
		sharedProperties.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
		sharedProperties.put("shared", properties.getShared());
		
		UserDto<ID> currentUser = LemonUtils.currentUser();
		if (currentUser != null)
			jwtService.addAuthHeader(response, currentUser.getUsername(),
					expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
		
		return LemonUtils.mapOf(
				"context", sharedProperties,
				"user", LemonUtils.currentUser());	
	}
	
	
	/**
	 * Signs up a user.
	 * 
	 * @param user	data fed by the user
	 */
	//@PreAuthorize("isAnonymous()")
	@Validated(SignUpValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid U user) {
		
		log.debug("Signing up user: " + user);

		initUser(user); // sets right all fields of the user
		userRepository.save(user);
		
		// if successfully committed
		LemonUtils.afterCommit(() -> {
		
			LemonUtils.login(user); // log the user in
			log.debug("Signed up user: " + user);
		});
	}
	
	
	/**
	 * Initializes the user based on the input data
	 * 
	 * @param user
	 */
	protected void initUser(U user) {
		
		log.debug("Initializing user: " + user);

		user.setPassword(passwordEncoder.encode(user.getPassword())); // encode the password
		makeUnverified(user); // make the user unverified
	}

	
	/**
	 * Makes a user unverified
	 * @param user
	 */
	protected void makeUnverified(U user) {
		
		user.getRoles().add(Role.UNVERIFIED);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		LemonUtils.afterCommit(() -> sendVerificationMail(user)); // send a verification mail to the user
	}
	
//	/***
//	 * Makes a user verified
//	 * @param user
//	 */
//	protected void makeVerified(U user) {
//		user.getRoles().remove(Role.UNVERIFIED);
//		user.setCredentialsUpdatedAt(new Date());
//		//user.setVerificationCode(null);
//	}
	
	
	/**
	 * Sends verification mail to a unverified user.
	 * 
	 * @param user
	 */
	protected void sendVerificationMail(final U user) {
		try {
			
			log.debug("Sending verification mail to: " + user);
			
			String verificationCode = jwtService.createToken(JwtService.VERIFY_AUDIENCE,
					user.getId().toString(), properties.getJwt().getExpirationMillis(),
					LemonUtils.mapOf("email", user.getEmail()));

			// make the link
			String verifyLink = properties.getApplicationUrl()
				+ "/users/" + user.getId() + "/verification?code=" + verificationCode;

			// send the mail
			sendVerificationMail(user, verifyLink);

			log.debug("Verification mail to " + user.getEmail() + " queued.");
			
		} catch (Throwable e) {
			// In case of exception, just log the error and keep silent
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}	

	
	/**
	 * Sends verification mail to a unverified user.
	 * Override this method if you're using a different MailData
	 */
	protected void sendVerificationMail(final U user, String verifyLink) {
		
		// send the mail
		mailSender.send(LemonMailData.of(user.getEmail(),
			LemonUtils.getMessage("com.naturalprogrammer.spring.verifySubject"),
			LemonUtils.getMessage(
				"com.naturalprogrammer.spring.verifyEmail",	verifyLink)));
	}	

	
	/**
	 * Resends verification mail to the user.
	 * 
	 * @param user
	 */
	@UserEditPermission
	public void resendVerificationMail(U user) {

		// The user must exist
		LemonUtils.ensureFound(user);
		
		// must be unverified
		LemonUtils.validate(user.getRoles().contains(Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	

		// send the verification mail
		sendVerificationMail(user);
	}

	
	/**
	 * Fetches a user by email
	 * 
	 * @param email
	 * @return the decorated user object
	 */
	public U fetchUserByEmail(@Valid @Email @NotBlank String email) {
		
		log.debug("Fetching user by email: " + email);
		return processUser(userRepository.findByEmail(email).orElse(null));
	}

	
	/**
	 * Returns a non-null, processed user for the client.
	 * 
	 * @param user
	 * @return
	 */
	public U processUser(U user) {
		
		log.debug("Fetching user: " + user);

		// ensure that the user exists
		LemonUtils.ensureFound(user);
		
		// hide confidential fields
		user.hideConfidentialFields();
		
		return user;
	}
	
	
	/**
	 * Verifies the email id of current-user
	 *  
	 * @param verificationCode
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void verifyUser(ID userId, String verificationCode) {
		
		log.debug("Verifying user ...");

		U user = userRepository.findById(userId).orElseThrow(LemonUtils.notFoundSupplier());
		
		// ensure that he is unverified
		LemonUtils.validate(user.hasRole(Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	
		
		JWTClaimsSet claims = jwtService.parseToken(verificationCode, JwtService.VERIFY_AUDIENCE, user.getCredentialsUpdatedMillis());
		
		LemonUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("email").equals(user.getEmail()),
				"com.naturalprogrammer.spring.wrong.verificationCode");
		
		user.getRoles().remove(Role.UNVERIFIED); // make him verified
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		userRepository.save(user);
		
		// after successful commit,
		LemonUtils.afterCommit(() -> {
			
			// Re-login the user, so that the UNVERIFIED role is removed
			LemonUtils.login(user);
			log.debug("Re-logged-in the user for removing UNVERIFIED role.");		
		});
		
		log.debug("Verified user: " + user);		
	}

	
	/**
	 * Forgot password.
	 * 
	 * @param email	the email of the user who forgot his password
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void forgotPassword(@Valid @Email @NotBlank String email) {
		
		log.debug("Processing forgot password for email: " + email);
		
		// fetch the user record from database
		U user = userRepository.findByEmail(email)
				.orElseThrow(LemonUtils.notFoundSupplier());

		mailForgotPasswordLink(user);
//		
//		// set a forgot password code
//		user.setForgotPasswordCode(LemonUtils.uid());
//		userRepository.save(user);
//
//		// after successful commit, mail him a link to reset his password
//		LemonUtils.afterCommit(() -> mailForgotPasswordLink(user));
	}
	
	
//	/**
//	 * Forgot password.
//	 * 
//	 * @param email	the email of the user who forgot his password
//	 */
//	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
//	public void forgotPassword(U user) {
//		
//		log.debug("Processing forgot password for user: " + user);
//		
//		// set a forgot password code
//		user.setForgotPasswordCode(LemonUtils.uid());
//		userRepository.save(user);
//
//		// after successful commit, mail him a link to reset his password
//		LemonUtils.afterCommit(() -> mailForgotPasswordLink(user));
//	}
//	
//	
	/**
	 * Mails the forgot password link.
	 * 
	 * @param user
	 */
	public void mailForgotPasswordLink(U user) {
		
		log.debug("Mailing forgot password link to user: " + user);

		String forgotPasswordCode = jwtService.createToken(JwtService.FORGOT_PASSWORD_AUDIENCE,
				user.getEmail(), properties.getJwt().getExpirationMillis());

		// make the link
		String forgotPasswordLink =	properties.getApplicationUrl()
			    + "/reset-password?code=" + forgotPasswordCode;
		
		mailForgotPasswordLink(user, forgotPasswordLink);
		
		log.debug("Forgot password link mail queued.");
	}

	
	/**
	 * Mails the forgot password link.
	 * 
	 * Override this method if you're using a different MailData
	 */
	public void mailForgotPasswordLink(U user, String forgotPasswordLink) {
		
		// send the mail
		mailSender.send(LemonMailData.of(user.getEmail(),
				LemonUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
				LemonUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail",
					forgotPasswordLink)));
	}
	
	/**
	 * Resets the password.
	 * 
	 * @param forgotPasswordCode
	 * @param newPassword
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void resetPassword(
			@Valid @NotBlank String forgotPasswordCode,
			@Valid @Password String newPassword) {
		
		log.debug("Resetting password ...");

		JWTClaimsSet claims = jwtService.parseToken(forgotPasswordCode,
				JwtService.FORGOT_PASSWORD_AUDIENCE);
		
		String email = claims.getSubject();
		
		// fetch the user
		U user = userRepository.findByEmail(email).orElseThrow(LemonUtils.notFoundSupplier());
		LemonUtils.ensureCredentialsUpToDate(claims, user);
		
		// sets the password
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		//user.setForgotPasswordCode(null);
		
		userRepository.save(user);
		
		// after successful commit,
		LemonUtils.afterCommit(() -> {
			
			// Login the user
			LemonUtils.login(user);
		});
		
		log.debug("Password reset.");		
	}

	
	/**
	 * Updates a user with the given data.
	 * 
	 * @param user
	 * @param updatedUser
	 */
	@UserEditPermission
	@Validated(AbstractUser.UpdateValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public UserDto<ID> updateUser(U user, @Valid U updatedUser) {
		
		log.debug("Updating user: " + user);

		// checks
		LemonUtils.ensureCorrectVersion(user, updatedUser);

		// delegates to updateUserFields
		updateUserFields(user, updatedUser, LemonUtils.currentUser());
		userRepository.save(user);
		
		log.debug("Updated user: " + user);
		
		UserDto<ID> userDto = user.toUserDto();
		userDto.setPassword(null);
		return userDto;
	}
	
	
	/**
	 * Changes the password.
	 * 
	 * @param user
	 * @param changePasswordForm
	 */
	@UserEditPermission
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public String changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password for user: " + user);
		
		// Get the old password of the logged in user (logged in user may be an ADMIN)
		UserDto<ID> currentUser = LemonUtils.currentUser();
		U loggedIn = userRepository.findById(currentUser.getId()).get();
		String oldPassword = loggedIn.getPassword();

		// checks
		LemonUtils.ensureFound(user);
		LemonUtils.validate("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
					oldPassword),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		// sets the password
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		userRepository.save(user);
		
//		// after successful commit
//		LemonUtils.afterCommit(() -> {
//
//			UserDto<ID> currentUser = LemonUtils.getSpringUser();
//			
////			if (currentUser.getId().equals(user.getId())) { // if current-user's password changed,
////				
////				log.debug("Logging out ...");
////				LemonUtils.logOut(); // log him out
////			}
//		});
//		
		log.debug("Changed password for user: " + user);
		return user.toUserDto().getUsername();
	}


	/**
	 * Updates the fields of the users. Override this if you have more fields.
	 * 
	 * @param user
	 * @param updatedUser
	 * @param currentUser
	 */
	protected void updateUserFields(U user, U updatedUser, UserDto<ID> currentUser) {

		log.debug("Updating user fields for user: " + user);

		// User is already decorated while checking the 'edit' permission
		// So, user.isRolesEditable() below would work
		
		// Another good admin must be logged in to edit roles
		if (currentUser.isGoodAdmin() &&
		   !currentUser.getId().equals(user.getId())) { 
			
			log.debug("Updating roles for user: " + user);

			// update the roles
			
			if (user.getRoles().equals(updatedUser.getRoles())) // roles are same
				return;
			
			if (updatedUser.hasRole(Role.UNVERIFIED)) {
				
				if (!user.hasRole(Role.UNVERIFIED)) {

					makeUnverified(user); // make user unverified
				}
			} else {
				
				if (user.hasRole(Role.UNVERIFIED))
					user.getRoles().remove(Role.UNVERIFIED); // make user verified
			}
			
			user.setRoles(updatedUser.getRoles());
			user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		}
	}

	
	/**
	 * Requests for email change.
	 * 
	 * @param user
	 * @param updatedUser
	 */
	@UserEditPermission
	@Validated(AbstractUser.ChangeEmailValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void requestEmailChange(U user, @Valid U updatedUser) {
		
		log.debug("Requesting email change: " + user);

		// checks
		LemonUtils.ensureFound(user);	
		LemonUtils.validate("updatedUser.password",
			passwordEncoder.matches(updatedUser.getPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();

		// preserves the new email id
		user.setNewEmail(updatedUser.getNewEmail());
		//user.setChangeEmailCode(LemonUtils.uid());
		userRepository.save(user);
		
		// after successful commit, mails a link to the user
		LemonUtils.afterCommit(() -> mailChangeEmailLink(user));
		
		log.debug("Requested email change: " + user);		
	}

	
	/**
	 * Mails the change-email verification link to the user.
	 */
	protected void mailChangeEmailLink(U user) {
		
		String changeEmailCode = jwtService.createToken(JwtService.CHANGE_EMAIL_AUDIENCE,
				user.getId().toString(), properties.getJwt().getExpirationMillis(),
				LemonUtils.mapOf("newEmail", user.getNewEmail()));
		
		try {
			
			log.debug("Mailing change email link to user: " + user);

			// make the link
			String changeEmailLink = properties.getApplicationUrl()
				    + "/users/" + user.getId()
					+ "/change-email?code=" + changeEmailCode;
			
			// mail it
			mailChangeEmailLink(user, changeEmailLink);
			
			log.debug("Change email link mail queued.");
			
		} catch (Throwable e) {
			// In case of exception, just log the error and keep silent			
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}


	/**
	 * Mails the change-email verification link to the user.
	 * 
	 * Override this method if you're using a different MailData
	 */
	protected void mailChangeEmailLink(U user, String changeEmailLink) {
		
		mailSender.send(LemonMailData.of(user.getNewEmail(),
			LemonUtils.getMessage(
				"com.naturalprogrammer.spring.changeEmailSubject"),
			LemonUtils.getMessage(
				"com.naturalprogrammer.spring.changeEmailEmail",
				 changeEmailLink)));
	}

	
	/**
	 * Change the email.
	 * 
	 * @param changeEmailCode
	 */
	@PreAuthorize("isAuthenticated()")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changeEmail(ID userId, @Valid @NotBlank String changeEmailCode) {
		
		log.debug("Changing email of current user ...");

		// fetch the current-user
		UserDto<ID> currentUser = LemonUtils.currentUser();
		
		LemonUtils.validate(userId.equals(currentUser.getId()),
			"com.naturalprogrammer.spring.wrong.login").go();
		
		U user = userRepository.findById(userId).orElseThrow(LemonUtils.notFoundSupplier());
		
		LemonUtils.validate(StringUtils.isNotBlank(user.getNewEmail()),
				"com.naturalprogrammer.spring.blank.newEmail").go();
		
		JWTClaimsSet claims = jwtService.parseToken(changeEmailCode,
				JwtService.CHANGE_EMAIL_AUDIENCE,
				user.getCredentialsUpdatedMillis());
		
		LemonUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("newEmail").equals(user.getNewEmail()),
				"com.naturalprogrammer.spring.wrong.changeEmailCode");
		
		// Ensure that the email would be unique 
		LemonUtils.validate(
				!userRepository.findByEmail(user.getNewEmail()).isPresent(),
				"com.naturalprogrammer.spring.duplicate.email").go();	
		
		// update the fields
		user.setEmail(user.getNewEmail());
		user.setNewEmail(null);
		//user.setChangeEmailCode(null);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		
		// make the user verified if he is not
		if (user.hasRole(Role.UNVERIFIED))
			user.getRoles().remove(Role.UNVERIFIED);
		
		userRepository.save(user);
		
		// after successful commit,
		LemonUtils.afterCommit(() -> {
			
			// Login the user
			LemonUtils.login(user);
		});
		// logout after successful commit
		//LemonUtils.afterCommit(LemonUtils::logOut);
		
		log.debug("Changed email of user: " + user);
	}


	public String getOAuth2Email(String registrationId, Map<String, Object> attributes) {

		return (String) attributes.get(StandardClaimNames.EMAIL);
	}
	
	public void fillAdditionalFields(String clientId, U user, Map<String, Object> attributes) {
		
	}

	public boolean getOAuth2AccountVerified(String registrationId, Map<String, Object> attributes) {

		Object verified = attributes.get(StandardClaimNames.EMAIL_VERIFIED);
		if (verified == null)
			verified = attributes.get("verified");
		
		return (boolean) verified;
	}

//	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
//	public void loginWithNonce(@Valid NonceForm<ID> nonce, HttpServletResponse response) {
//		
//		U user = userRepository.findById(nonce.getUserId())
//			.orElseThrow(LemonUtils.notFoundSupplier());
//
//		LemonUtils.ensureCredentials(nonce.getNonce().equals(user.getNonce()),
//			"com.naturalprogrammer.spring.invalidNonce");
//
//		user.setNonce(null);
//		userRepository.save(user);
//		LemonUtils.login(user);
//	}

//	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
//	public String addNonce(U user) {
//		
//		String nonce = LemonUtils.uid();
//		user.setNonce(nonce);
//		userRepository.save(user);
//		
//		return nonce;
//	}
//
	/**
	 * Fetches a new token - for session scrolling etc.
	 * @return 
	 */
	@PreAuthorize("isAuthenticated()")
	public String fetchNewToken(Optional<Long> expirationMillis,
			Optional<String> optionalUsername) {
		
		UserDto<ID> currentUser = LemonUtils.currentUser();
		String username = optionalUsername.orElse(currentUser.getUsername());
		
		LemonUtils.ensureAuthority(currentUser.getUsername().equals(username) ||
				currentUser.isGoodAdmin(), "com.naturalprogrammer.spring.notGoodAdminOrSameUser");
		
		return LemonSecurityConfig.TOKEN_PREFIX +
				jwtService.createToken(JwtService.AUTH_AUDIENCE, username,
				expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void save(U user) {
		
		userRepository.save(user);
	}
}
