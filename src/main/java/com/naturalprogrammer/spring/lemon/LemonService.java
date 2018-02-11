package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.forms.NonceForm;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.permissions.UserEditPermission;
import com.naturalprogrammer.spring.lemon.security.JwtService;
import com.naturalprogrammer.spring.lemon.security.SpringUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.Password;

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
			MailSender mailSender,
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
	 */
	public Map<String, Object> getContext() {
		
		// make the context
		Map<String, Object> context = new HashMap<String, Object>(2);
		context.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
		context.put("shared", properties.getShared());
		
		log.debug("Returning context: " + context);

		return context;		
	}
	
	
	/**
	 * Signs up a user.
	 * 
	 * @param user	data fed by the user
	 */
	@PreAuthorize("isAnonymous()")
	@Validated(SignUpValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid U user) {
		
		log.debug("Signing up user: " + user);

		initUser(user); // sets right all fields of the user
		userRepository.save(user);
		
		// if successfully committed
		LemonUtils.afterCommit(() -> {
		
			LemonUtils.logIn(user); // log the user in
			sendVerificationMail(user); // send verification mail
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
		user.setVerificationCode(LemonUtils.uid());
	}
	
	
	/***
	 * Makes a user verified
	 * @param user
	 */
	protected void makeVerified(U user) {
		user.getRoles().remove(Role.UNVERIFIED);
		user.setVerificationCode(null);
	}
	
	
	/**
	 * Sends verification mail to a unverified user.
	 * 
	 * @param user
	 */
	protected void sendVerificationMail(final U user) {
		try {
			
			log.debug("Sending verification mail to: " + user);

			// make the link
			String verifyLink = properties.getApplicationUrl()
				+ "/users/" + user.getVerificationCode() + "/verify";
			
			// send the mail
			mailSender.send(user.getEmail(),
				LemonUtils.getMessage("com.naturalprogrammer.spring.verifySubject"),
				LemonUtils.getMessage(
					"com.naturalprogrammer.spring.verifyEmail",	verifyLink));
			
			log.debug("Verification mail to " + user.getEmail() + " queued.");
			
		} catch (MessagingException e) {
			// In case of exception, just log the error and keep silent
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}	

	
	/**
	 * Resends verification mail to the user.
	 * 
	 * @param user
	 */
	@UserEditPermission
	public void resendVerificationMail(U user) {

		// The user must exist
		LemonUtils.check("id", user != null,
				"com.naturalprogrammer.spring.userNotFound").go();
		
		// must be unverified
		LemonUtils.check(user.getRoles().contains(Role.UNVERIFIED),
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

		// fetch the user
		U user = userRepository.findByEmail(email)
			.orElseThrow(MultiErrorException.supplier("email",
				"com.naturalprogrammer.spring.userNotFound"));

		// hide confidential fields
		user.hideConfidentialFields();
		
		log.debug("Returning user: " + user);		

		return user;
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
		LemonUtils.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		
		// hide confidential fields
		user.hideConfidentialFields();
		
		return user;
	}
	
	
	/**
	 * Verifies the email id of current-user
	 *  
	 * @param verificationCode
	 */
	@PreAuthorize("isAuthenticated()")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void verifyUser(@Valid @NotBlank String verificationCode) {
		
		log.debug("Verifying user ...");

		// fetch a fresh copy from the database
		U user = userRepository.getOne((ID) LemonUtils.getSpringUser().getId());
		
		// ensure that he is unverified
		LemonUtils.check(user.hasRole(Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	
		
		// ensure that the verification code of the user matches with the given one
		LemonUtils.check(verificationCode.equals(user.getVerificationCode()),
				"com.naturalprogrammer.spring.wrong.verificationCode").go();
		
		makeVerified(user); // make him verified
		userRepository.save(user);
		
		// after successful commit,
		LemonUtils.afterCommit(() -> {
			
			// Re-login the user, so that the UNVERIFIED role is removed
			LemonUtils.logIn(user);
			
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
				.orElseThrow(MultiErrorException.supplier(
					"com.naturalprogrammer.spring.userNotFound"));

		// set a forgot password code
		user.setForgotPasswordCode(LemonUtils.uid());
		userRepository.save(user);

		// after successful commit, mail him a link to reset his password
		LemonUtils.afterCommit(() -> mailForgotPasswordLink(user));
	}
	
	
	/**
	 * Forgot password.
	 * 
	 * @param email	the email of the user who forgot his password
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void forgotPassword(U user) {
		
		log.debug("Processing forgot password for user: " + user);
		
		// set a forgot password code
		user.setForgotPasswordCode(LemonUtils.uid());
		userRepository.save(user);

		// after successful commit, mail him a link to reset his password
		LemonUtils.afterCommit(() -> mailForgotPasswordLink(user));
	}
	
	
	/**
	 * Mails the forgot password link.
	 * 
	 * @param user
	 */
	protected void mailForgotPasswordLink(U user) {
		
		try {

			log.debug("Mailing forgot password link to user: " + user);

			// make the link
			String forgotPasswordLink =	properties.getApplicationUrl()
				    + "/users/" + user.getForgotPasswordCode()
					+ "/reset-password";
			
			// send the mail
			mailSender.send(user.getEmail(),
					LemonUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
					LemonUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail",
						forgotPasswordLink));
			
			log.debug("Forgot password link mail queued.");
			
		} catch (MessagingException e) {
			// In case of exception, just log the error and keep silent			
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}

	
	/**
	 * Resets the password.
	 * 
	 * @param forgotPasswordCode
	 * @param newPassword
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void resetPassword(@Valid @NotBlank String forgotPasswordCode,
							  @Valid @Password String newPassword) {
		
		log.debug("Resetting password ...");

		// fetch the user
		U user = userRepository
			.findByForgotPasswordCode(forgotPasswordCode)
			.orElseThrow(MultiErrorException.supplier(
				"com.naturalprogrammer.spring.invalidLink"));
		
		// sets the password
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setForgotPasswordCode(null);
		
		userRepository.save(user);
		
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
	public void updateUser(U user, @Valid U updatedUser) {
		
		log.debug("Updating user: " + user);

		// checks
		LemonUtils.validateVersion(user, updatedUser);

		// delegates to updateUserFields
		updateUserFields(user, updatedUser, LemonUtils.getSpringUser());
		userRepository.save(user);
		
		log.debug("Updated user: " + user);		
	}
	
	
	/**
	 * Changes the password.
	 * 
	 * @param user
	 * @param changePasswordForm
	 */
	@UserEditPermission
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password for user: " + user);

		// checks
		LemonUtils.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		LemonUtils.check("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		// sets the password
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		user.setCredentialsUpdatedAt(new Date());
		userRepository.save(user);
		
		// after successful commit
		LemonUtils.afterCommit(() -> {

			SpringUser<ID> currentUser = LemonUtils.getSpringUser();
			
			if (currentUser.getId().equals(user.getId())) { // if current-user's password changed,
				
				log.debug("Logging out ...");
				LemonUtils.logOut(); // log him out
			}
		});
		
		log.debug("Changed password for user: " + user);
	}


	/**
	 * Updates the fields of the users. Override this if you have more fields.
	 * 
	 * @param user
	 * @param updatedUser
	 * @param currentUser
	 */
	protected void updateUserFields(U user, U updatedUser, SpringUser<ID> currentUser) {

		log.debug("Updating user fields for user: " + user);

		// User is already decorated while checking the 'edit' permission
		// So, user.isRolesEditable() below would work
		
		// Another good admin must be logged in to edit roles
		if (currentUser.isGoodAdmin() &&
		   !currentUser.getId().equals(user.getId())) { 
			
			log.debug("Updating roles for user: " + user);

			// update the roles
			
			if (updatedUser.hasRole(Role.UNVERIFIED)) {
				
				if (!user.hasRole(Role.UNVERIFIED)) {

					makeUnverified(user); // make user unverified
					LemonUtils.afterCommit(() -> sendVerificationMail(user)); // send a verification mail to the user
				}
			} else {
				
				if (user.hasRole(Role.UNVERIFIED))
					makeVerified(user); // make user verified
			}
			
			user.setRoles(updatedUser.getRoles());
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
		LemonUtils.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
	
		LemonUtils.check("updatedUser.password",
			passwordEncoder.matches(updatedUser.getPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();

		// preserves the new email id
		user.setNewEmail(updatedUser.getNewEmail());
		user.setChangeEmailCode(LemonUtils.uid());
		userRepository.save(user);
		
		// after successful commit, mails a link to the user
		LemonUtils.afterCommit(() -> mailChangeEmailLink(user));
		
		log.debug("Requested email change: " + user);		
	}

	
	/**
	 * Mails the change-email verification link to the user.
	 * 
	 * @param user
	 */
	protected void mailChangeEmailLink(U user) {
		
		try {
			
			log.debug("Mailing change email link to user: " + user);

			// make the link
			String changeEmailLink = properties.getApplicationUrl()
				    + "/users/" + user.getChangeEmailCode()
					+ "/change-email";
			
			// mail it
			mailSender.send(user.getEmail(),
				LemonUtils.getMessage(
					"com.naturalprogrammer.spring.changeEmailSubject"),
				LemonUtils.getMessage(
					"com.naturalprogrammer.spring.changeEmailEmail",
					 changeEmailLink));
			
			log.debug("Change email link mail queued.");
			
		} catch (MessagingException e) {
			// In case of exception, just log the error and keep silent			
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}


	/**
	 * Change the email.
	 * 
	 * @param changeEmailCode
	 */
	@PreAuthorize("isAuthenticated()")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changeEmail(@Valid @NotBlank String changeEmailCode) {
		
		log.debug("Changing email of current user ...");

		// fetch the current-user
		SpringUser<ID> currentUser = LemonUtils.getSpringUser();
		U user = userRepository.getOne(currentUser.getId());
		
		// checks
		
		LemonUtils.check(changeEmailCode.equals(user.getChangeEmailCode()),
				"com.naturalprogrammer.spring.wrong.changeEmailCode").go();
		
		// Ensure that the email would be unique 
		LemonUtils.check(
				!userRepository.findByEmail(user.getNewEmail()).isPresent(),
				"com.naturalprogrammer.spring.duplicate.email").go();	
		
		// update the fields
		user.setEmail(user.getNewEmail());
		user.setNewEmail(null);
		user.setChangeEmailCode(null);
		user.setCredentialsUpdatedAt(new Date());
		
		// make the user verified if he is not
		if (user.hasRole(Role.UNVERIFIED))
			makeVerified(user);
		
		userRepository.save(user);
		
		// logout after successful commit
		LemonUtils.afterCommit(LemonUtils::logOut);
		
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

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public SpringUser<ID> loginWithNonce(@Valid NonceForm<ID> nonce, HttpServletResponse response) {
		
		U user = userRepository.findById(nonce.getUserId())
			.orElseThrow(MultiErrorException.supplier(
				"com.naturalprogrammer.spring.userNotFound"));

		if (user.getNonce().equals(nonce.getNonce())) {
			
			user.setNonce(null);
			userRepository.save(user);
			
			if (nonce.getExpirationMilli() == null)
				nonce.setExpirationMilli(properties.getJwt().getExpirationMilli());
			
			jwtService.addJwtAuthHeader(response,
					user.toSpringUser().getUsername(),
					nonce.getExpirationMilli());

			return user.toSpringUser();
		}
		
		throw MultiErrorException.supplier("com.naturalprogrammer.spring.invalidNonce").get();
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public String addNonce(U user) {
		
		String nonce = LemonUtils.uid();
		user.setNonce(nonce);
		userRepository.save(user);
		
		return nonce;
	}


	/**
	 * Fetches a new token - for session scrolling etc.
	 */
	@PreAuthorize("isAuthenticated()")
	public SpringUser<ID> fetchNewToken(Optional<Long> expirationMillis, HttpServletResponse response) {
		
		SpringUser<ID> user = LemonUtils.getSpringUser();
		
		jwtService.addJwtAuthHeader(response,
				user.getUsername(),
				expirationMillis.orElse(properties.getJwt().getExpirationMilli()));
		
		return user;
	}
}
