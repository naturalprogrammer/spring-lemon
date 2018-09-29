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

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties.Admin;
import com.naturalprogrammer.spring.lemon.commons.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.naturalprogrammer.spring.lemon.commons.mail.LemonMailData;
import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.security.UserEditPermission;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.commonsjpa.LecjUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * The Lemon Service class
 * 
 * @author Sanjay Patel
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
		user.getRoles().add(UserUtils.Role.ADMIN);
		
		return user;
	}

    
	/**
	 * Creates a new user object. Must be overridden in the
	 * subclass, like this:
	 * 
	 * <pre>
	 * public User newUser() {
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
	 * If a user is logged in, it also returns the user data
	 * and a new authorization token. If expirationMillis is not provided,
	 * the expiration of the new token is set to the default.
	 *
	 * Override this method if needed.
	 */
	public Map<String, Object> getContext(Optional<Long> expirationMillis, HttpServletResponse response) {
		
		log.debug("Getting context ...");

		// make the context
		Map<String, Object> sharedProperties = new HashMap<String, Object>(2);
		sharedProperties.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
		sharedProperties.put("shared", properties.getShared());
		
		UserDto currentUser = LecwUtils.currentUser();
		if (currentUser != null)
			addAuthHeader(response, currentUser.getUsername(),
				expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
		
		return LecUtils.mapOf(
				"context", sharedProperties,
				"user", LecwUtils.currentUser());	
	}
	
	
	/**
	 * Signs up a user.
	 */
	@Validated(UserUtils.SignUpValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid U user) {
		
		log.debug("Signing up user: " + user);

		initUser(user); // sets right all fields of the user
		userRepository.save(user);
		
		// if successfully committed
		LecjUtils.afterCommit(() -> {
		
			LemonUtils.login(user); // log the user in
			log.debug("Signed up user: " + user);
		});
	}
	
	
	/**
	 * Initializes the user based on the input data,
	 * e.g. encrypts the password
	 */
	protected void initUser(U user) {
		
		log.debug("Initializing user: " + user);

		user.setPassword(passwordEncoder.encode(user.getPassword())); // encode the password
		makeUnverified(user); // make the user unverified
	}

	
	/**
	 * Makes a user unverified
	 */
	protected void makeUnverified(U user) {
		
		user.getRoles().add(UserUtils.Role.UNVERIFIED);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		LecjUtils.afterCommit(() -> sendVerificationMail(user)); // send a verification mail to the user
	}
	
	
	/**
	 * Sends verification mail to a unverified user.
	 */
	protected void sendVerificationMail(final U user) {
		try {
			
			log.debug("Sending verification mail to: " + user);
			
			String verificationCode = jwtService.createToken(JwtService.VERIFY_AUDIENCE,
					user.getId().toString(), properties.getJwt().getExpirationMillis(),
					LecUtils.mapOf("email", user.getEmail()));

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
			LexUtils.getMessage("com.naturalprogrammer.spring.verifySubject"),
			LexUtils.getMessage(
				"com.naturalprogrammer.spring.verifyEmail",	verifyLink)));
	}	

	
	/**
	 * Resends verification mail to the user.
	 */
	@UserEditPermission
	public void resendVerificationMail(U user) {

		// The user must exist
		LexUtils.ensureFound(user);
		
		// must be unverified
		LexUtils.validate(user.getRoles().contains(UserUtils.Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	

		// send the verification mail
		sendVerificationMail(user);
	}

	
	/**
	 * Fetches a user by email
	 */
	public U fetchUserByEmail(@Valid @Email @NotBlank String email) {
		
		log.debug("Fetching user by email: " + email);
		return processUser(userRepository.findByEmail(email).orElse(null));
	}

	
	/**
	 * Returns a non-null, processed user for the client.
	 */
	public U processUser(U user) {
		
		log.debug("Fetching user: " + user);

		// ensure that the user exists
		LexUtils.ensureFound(user);
		
		// hide confidential fields
		hideConfidentialFields(user);
		
		return user;
	}
	
	
	/**
	 * Verifies the email id of current-user
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void verifyUser(ID userId, String verificationCode) {
		
		log.debug("Verifying user ...");

		U user = userRepository.findById(userId).orElseThrow(LexUtils.notFoundSupplier());
		
		// ensure that he is unverified
		LexUtils.validate(user.hasRole(UserUtils.Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	
		
		JWTClaimsSet claims = jwtService.parseToken(verificationCode, JwtService.VERIFY_AUDIENCE, user.getCredentialsUpdatedMillis());
		
		LecUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("email").equals(user.getEmail()),
				"com.naturalprogrammer.spring.wrong.verificationCode");
		
		user.getRoles().remove(UserUtils.Role.UNVERIFIED); // make him verified
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		userRepository.save(user);
		
		// after successful commit,
		LecjUtils.afterCommit(() -> {
			
			// Re-login the user, so that the UNVERIFIED role is removed
			LemonUtils.login(user);
			log.debug("Re-logged-in the user for removing UNVERIFIED role.");		
		});
		
		log.debug("Verified user: " + user);		
	}

	
	/**
	 * Forgot password.
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void forgotPassword(@Valid @Email @NotBlank String email) {
		
		log.debug("Processing forgot password for email: " + email);
		
		// fetch the user record from database
		U user = userRepository.findByEmail(email)
				.orElseThrow(LexUtils.notFoundSupplier());

		mailForgotPasswordLink(user);
	}
	
	
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
				LexUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
				LexUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail",
					forgotPasswordLink)));
	}
	
	/**
	 * Resets the password.
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void resetPassword(@Valid ResetPasswordForm form) {
		
		log.debug("Resetting password ...");

		JWTClaimsSet claims = jwtService.parseToken(form.getCode(),
				JwtService.FORGOT_PASSWORD_AUDIENCE);
		
		String email = claims.getSubject();
		
		// fetch the user
		U user = userRepository.findByEmail(email).orElseThrow(LexUtils.notFoundSupplier());
		LemonUtils.ensureCredentialsUpToDate(claims, user);
		
		// sets the password
		user.setPassword(passwordEncoder.encode(form.getNewPassword()));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		//user.setForgotPasswordCode(null);
		
		userRepository.save(user);
		
		// after successful commit,
		LecjUtils.afterCommit(() -> {
			
			// Login the user
			LemonUtils.login(user);
		});
		
		log.debug("Password reset.");		
	}

	
	/**
	 * Updates a user with the given data.
	 */
	@UserEditPermission
	@Validated(UserUtils.UpdateValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public UserDto updateUser(U user, @Valid U updatedUser) {
		
		log.debug("Updating user: " + user);

		// checks
		LemonUtils.ensureCorrectVersion(user, updatedUser);

		// delegates to updateUserFields
		updateUserFields(user, updatedUser, LecwUtils.currentUser());
		userRepository.save(user);
		
		log.debug("Updated user: " + user);
		
		UserDto userDto = user.toUserDto();
		userDto.setPassword(null);
		return userDto;
	}
	
	
	/**
	 * Changes the password.
	 */
	@UserEditPermission
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public String changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password for user: " + user);
		
		// Get the old password of the logged in user (logged in user may be an ADMIN)
		UserDto currentUser = LecwUtils.currentUser();
		U loggedIn = userRepository.findById(toId(currentUser.getId())).get();
		String oldPassword = loggedIn.getPassword();

		// checks
		LexUtils.ensureFound(user);
		LexUtils.validate("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
					oldPassword),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		// sets the password
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		userRepository.save(user);

		log.debug("Changed password for user: " + user);
		return user.toUserDto().getUsername();
	}


	protected abstract ID toId(String id);

	/**
	 * Updates the fields of the users. Override this if you have more fields.
	 */
	protected void updateUserFields(U user, U updatedUser, UserDto currentUser) {

		log.debug("Updating user fields for user: " + user);

		// Another good admin must be logged in to edit roles
		if (currentUser.isGoodAdmin() &&
		   !currentUser.getId().equals(user.getId().toString())) { 
			
			log.debug("Updating roles for user: " + user);

			// update the roles
			
			if (user.getRoles().equals(updatedUser.getRoles())) // roles are same
				return;
			
			if (updatedUser.hasRole(UserUtils.Role.UNVERIFIED)) {
				
				if (!user.hasRole(UserUtils.Role.UNVERIFIED)) {

					makeUnverified(user); // make user unverified
				}
			} else {
				
				if (user.hasRole(UserUtils.Role.UNVERIFIED))
					user.getRoles().remove(UserUtils.Role.UNVERIFIED); // make user verified
			}
			
			user.setRoles(updatedUser.getRoles());
			user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		}
	}

	
	/**
	 * Requests for email change.
	 */
	@UserEditPermission
	@Validated(UserUtils.ChangeEmailValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void requestEmailChange(U user, @Valid U updatedUser) {
		
		log.debug("Requesting email change: " + user);

		// checks
		LexUtils.ensureFound(user);	
		LexUtils.validate("updatedUser.password",
			passwordEncoder.matches(updatedUser.getPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();

		// preserves the new email id
		user.setNewEmail(updatedUser.getNewEmail());
		//user.setChangeEmailCode(LemonUtils.uid());
		userRepository.save(user);
		
		// after successful commit, mails a link to the user
		LecjUtils.afterCommit(() -> mailChangeEmailLink(user));
		
		log.debug("Requested email change: " + user);		
	}

	
	/**
	 * Mails the change-email verification link to the user.
	 */
	protected void mailChangeEmailLink(U user) {
		
		String changeEmailCode = jwtService.createToken(JwtService.CHANGE_EMAIL_AUDIENCE,
				user.getId().toString(), properties.getJwt().getExpirationMillis(),
				LecUtils.mapOf("newEmail", user.getNewEmail()));
		
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
				LexUtils.getMessage(
				"com.naturalprogrammer.spring.changeEmailSubject"),
				LexUtils.getMessage(
				"com.naturalprogrammer.spring.changeEmailEmail",
				 changeEmailLink)));
	}

	
	/**
	 * Change the email.
	 */
	@PreAuthorize("isAuthenticated()")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changeEmail(ID userId, @Valid @NotBlank String changeEmailCode) {
		
		log.debug("Changing email of current user ...");

		// fetch the current-user
		UserDto currentUser = LecwUtils.currentUser();
		
		LexUtils.validate(userId.equals(toId(currentUser.getId())),
			"com.naturalprogrammer.spring.wrong.login").go();
		
		U user = userRepository.findById(userId).orElseThrow(LexUtils.notFoundSupplier());
		
		LexUtils.validate(StringUtils.isNotBlank(user.getNewEmail()),
				"com.naturalprogrammer.spring.blank.newEmail").go();
		
		JWTClaimsSet claims = jwtService.parseToken(changeEmailCode,
				JwtService.CHANGE_EMAIL_AUDIENCE,
				user.getCredentialsUpdatedMillis());
		
		LecUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("newEmail").equals(user.getNewEmail()),
				"com.naturalprogrammer.spring.wrong.changeEmailCode");
		
		// Ensure that the email would be unique 
		LexUtils.validate(
				!userRepository.findByEmail(user.getNewEmail()).isPresent(),
				"com.naturalprogrammer.spring.duplicate.email").go();	
		
		// update the fields
		user.setEmail(user.getNewEmail());
		user.setNewEmail(null);
		//user.setChangeEmailCode(null);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		
		// make the user verified if he is not
		if (user.hasRole(UserUtils.Role.UNVERIFIED))
			user.getRoles().remove(UserUtils.Role.UNVERIFIED);
		
		userRepository.save(user);
		
		// after successful commit,
		LecjUtils.afterCommit(() -> {
			
			// Login the user
			LemonUtils.login(user);
		});
		
		log.debug("Changed email of user: " + user);
	}


	/**
	 * Extracts the email id from user attributes received from OAuth2 provider, e.g. Google
	 * 
	 */
	public String getOAuth2Email(String registrationId, Map<String, Object> attributes) {

		return (String) attributes.get(StandardClaimNames.EMAIL);
	}

	
	/**
	 * Extracts additional fields, e.g. name from user attributes received from OAuth2 provider, e.g. Google
	 * Override this if you introduce more user fields, e.g. name
	 */
	public void fillAdditionalFields(String clientId, U user, Map<String, Object> attributes) {
		
	}

	
	/**
	 * Checks if the account at the OAuth2 provider is verified 
	 */
	public boolean getOAuth2AccountVerified(String registrationId, Map<String, Object> attributes) {

		Object verified = attributes.get(StandardClaimNames.EMAIL_VERIFIED);
		if (verified == null)
			verified = attributes.get("verified");
		
		return (boolean) verified;
	}


	/**
	 * Fetches a new token - for session scrolling etc.
	 * @return 
	 */
	@PreAuthorize("isAuthenticated()")
	public String fetchNewToken(Optional<Long> expirationMillis,
			Optional<String> optionalUsername) {
		
		UserDto currentUser = LecwUtils.currentUser();
		String username = optionalUsername.orElse(currentUser.getUsername());
		
		LecUtils.ensureAuthority(currentUser.getUsername().equals(username) ||
				currentUser.isGoodAdmin(), "com.naturalprogrammer.spring.notGoodAdminOrSameUser");
		
		return LecUtils.TOKEN_PREFIX +
				jwtService.createToken(JwtService.AUTH_AUDIENCE, username,
				expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
	}

	
	/**
	 * Saves the user
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void save(U user) {
		
		userRepository.save(user);
	}
	
	
	/**
	 * Hides the confidential fields before sending to client
	 */
	protected void hideConfidentialFields(U user) {
		
		user.setPassword(null); // JsonIgnore didn't work
		
		if (!user.hasPermission(LecwUtils.currentUser(), UserUtils.Permission.EDIT))
			user.setEmail(null);
		
		log.debug("Hid confidential fields for user: " + user);
	}

	
	/**
	 * Adds a Lemon-Authorization header to the response
	 */
	public void addAuthHeader(HttpServletResponse response, String username, Long expirationMillis) {
	
		response.addHeader(LecUtils.TOKEN_RESPONSE_HEADER_NAME,
				LecUtils.TOKEN_PREFIX +
				jwtService.createToken(JwtService.AUTH_AUDIENCE, username, expirationMillis));
	}
	
	public Optional<U> findUserById(String id) {
		return userRepository.findById(toId(id));
	}
}
