package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.permissions.UserEditPermission;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;
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

	@Autowired
	public void createLemonService(LemonProperties properties,
			PasswordEncoder passwordEncoder,
			MailSender mailSender,
			AbstractUserRepository<U, ID> userRepository,
			UserDetailsService userDetailsService) {
		
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
		this.userRepository = userRepository;
		this.userDetailsService = userDetailsService;
		
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
		user.setUsername(initialAdmin.getUsername());
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
		LemonUtil.afterCommit(() -> {
		
			LemonUtil.logIn(user); // log the user in
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
		user.setVerificationCode(LemonUtil.uid());
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
				LemonUtil.getMessage("com.naturalprogrammer.spring.verifySubject"),
				LemonUtil.getMessage(
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
		LemonUtil.check("id", user != null,
				"com.naturalprogrammer.spring.userNotFound").go();
		
		// must be unverified
		LemonUtil.check(user.getRoles().contains(Role.UNVERIFIED),
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

		// decorate the user, and hide confidential fields
		user.decorate().hideConfidentialFields();
		
		log.debug("Returning user: " + user);		

		return user;
	}

	
	/**
	 * Returns a non-null, decorated user for the client.
	 * 
	 * @param user
	 * @return
	 */
	public U processUser(U user) {
		
		log.debug("Fetching user: " + user);

		// ensure that the user exists
		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		
		// decorate the user, and hide confidential fields
		user.decorate().hideConfidentialFields();
		
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

		// get the current-user from the session
		U currentUser = LemonUtil.getUser();
		
		// fetch a fresh copy from the database
		U user = userRepository.findOne(currentUser.getId());
		
		// ensure that he is unverified
		LemonUtil.check(user.getRoles().contains(Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	
		
		// ensure that the verification code of the user matches with the given one
		LemonUtil.check(verificationCode.equals(user.getVerificationCode()),
				"com.naturalprogrammer.spring.wrong.verificationCode").go();
		
		makeVerified(user); // make him verified
		userRepository.save(user);
		
		// after successful commit,
		LemonUtil.afterCommit(() -> {
			
			// Re-login the user, so that the UNVERIFIED role is removed
			LemonUtil.logIn(user);
			
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
		user.setForgotPasswordCode(LemonUtil.uid());
		userRepository.save(user);

		// after successful commit, mail him a link to reset his password
		LemonUtil.afterCommit(() -> mailForgotPasswordLink(user));
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
		user.setForgotPasswordCode(LemonUtil.uid());
		userRepository.save(user);

		// after successful commit, mail him a link to reset his password
		LemonUtil.afterCommit(() -> mailForgotPasswordLink(user));
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
					LemonUtil.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
					LemonUtil.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail",
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
		LemonUtil.validateVersion(user, updatedUser);

		// delegates to updateUserFields
		updateUserFields(user, updatedUser, LemonUtil.getUser());
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
		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.check("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		// sets the password
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		userRepository.save(user);
		
		// after successful commit
		LemonUtil.afterCommit(() -> {

			U currentUser = LemonUtil.getUser();
			
			if (currentUser.equals(user)) { // if current-user's password changed,
				
				log.debug("Logging out ...");
				LemonUtil.logOut(); // log him out
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
	protected void updateUserFields(U user, U updatedUser, U currentUser) {

		log.debug("Updating user fields for user: " + user);

		// User is already decorated while checking the 'edit' permission
		// So, user.isRolesEditable() below would work
		if (user.isRolesEditable()) { 
			
			log.debug("Updating roles for user: " + user);

			// update the roles
			
			Set<String> roles = user.getRoles();
			
			if (updatedUser.isUnverified()) {
				
				if (!user.hasRole(Role.UNVERIFIED)) {

					makeUnverified(user); // make user unverified
					LemonUtil.afterCommit(() -> sendVerificationMail(user)); // send a verification mail to the user
				}
			} else {
				
				if (user.hasRole(Role.UNVERIFIED))
					makeVerified(user); // make user verified
			}
			
			if (updatedUser.isAdmin())
				roles.add(Role.ADMIN);
			else
				roles.remove(Role.ADMIN);
			
			if (updatedUser.isBlocked())
				roles.add(Role.BLOCKED);
			else
				roles.remove(Role.BLOCKED);
		}
	}

	
	/**
	 * Gets the current-user to be sent to a client.
	 * 
	 * @return
	 */
	public U userForClient() {
		
		// delegates
		return userForClient(LemonUtil.getUser());
	}

	
	/**
	 * Gets the current-user to be sent to a client.
	 * Override this if you have more fields.
	 * 
	 * @param currentUser
	 */
	protected U userForClient(U currentUser) {
		
		if (currentUser == null)
			return null;
		
		U user = newUser();
		user.setIdForClient(currentUser.getId());
		user.setUsername(currentUser.getUsername());
		user.setRoles(currentUser.getRoles());
		user.decorate(currentUser);
		
		log.debug("Returning user for client: " + user);
		
		return user;
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
		LemonUtil.check("id", user != null,
				"com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.check("updatedUser.password",
			passwordEncoder.matches(updatedUser.getPassword(),
									LemonUtil.getUser().getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();

		// preserves the new email id
		user.setNewEmail(updatedUser.getNewEmail());
		user.setChangeEmailCode(LemonUtil.uid());
		userRepository.save(user);
		
		// after successful commit, mails a link to the user
		LemonUtil.afterCommit(() -> mailChangeEmailLink(user));
		
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
				LemonUtil.getMessage(
					"com.naturalprogrammer.spring.changeEmailSubject"),
				LemonUtil.getMessage(
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
		U currentUser = LemonUtil.getUser();
		U user = userRepository.findOne(currentUser.getId());
		
		// checks
		
		LemonUtil.check(changeEmailCode.equals(user.getChangeEmailCode()),
				"com.naturalprogrammer.spring.wrong.changeEmailCode").go();
		
		// Ensure that the email would be unique 
		LemonUtil.check(
				!userRepository.findByEmail(user.getNewEmail()).isPresent(),
				"com.naturalprogrammer.spring.duplicate.email").go();	
		
		// update the fields
		user.setEmail(user.getNewEmail());
		user.setNewEmail(null);
		user.setChangeEmailCode(null);
		
		// make the user verified if he is not
		if (user.hasRole(Role.UNVERIFIED))
			makeVerified(user);
		
		userRepository.save(user);
		
		// logout after successful commit
		LemonUtil.afterCommit(LemonUtil::logOut);
		
		log.debug("Changed email of user: " + user);		
	}


	@UserEditPermission
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Map<String, String> createToken(U user) {
		
		log.debug("Creating token for user: " + user);

		// checks
		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		
		// set token
		String token = LemonUtil.uid();
		user.setApiKey(passwordEncoder.encode(token));
		userRepository.save(user);

		log.debug("Created token for user: " + user);	
		return LemonUtil.mapOf("token", token);
	}


	@UserEditPermission
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void removeToken(U user) {
		
		log.debug("Removing token for user: " + user);

		// checks
		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		
		// remove the token
		user.setApiKey(null);
		userRepository.save(user);

		log.debug("Removed token for user: " + user);	
	}

	abstract public ID parseId(String id);


	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public U extractPrincipal(Map<String, Object> map, String emailKey) {
		
		U user = newUser();
		user.setEmail((String) map.get(emailKey));
		user.setUsername(user.getEmail());
		user.setPassword(passwordEncoder.encode(LemonUtil.uid()));
		
		fillAdditionalFields(user, map);

		// set a forgot password code
		user.setForgotPasswordCode(LemonUtil.uid());
		userRepository.save(user);

		// after successful commit, mail him a link to reset his password
		LemonUtil.afterCommit(() -> mailForgotPasswordLink(user));
		
		return user.decorate(user);
	}

	protected void fillAdditionalFields(U user, Map<String, Object> map) {
		// Override for filling any additional fields, e.g. name
	}	
}
