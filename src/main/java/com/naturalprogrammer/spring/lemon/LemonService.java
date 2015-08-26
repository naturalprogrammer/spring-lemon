package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
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
    public void setProperties(LemonProperties properties) {
		this.properties = properties;
	}

	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Autowired
	public void setUserRepository(AbstractUserRepository<U, ID> userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	
	/**
     * This method is called after the context is built.
     * Needs to be public - otherwise Spring screams.
     * 
     * @param event
     */
    @EventListener
    public void afterContextRefreshed(ContextRefreshedEvent event) {
    	
    	log.info("Starting up Spring Lemon ...");
    	onStartup(); // delegate to onStartup()
    	log.info("Spring Lemon started");
    	
    }

    
	/**
	 * Creates a the initial Admin user, if not found.
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
    abstract protected U newUser();


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
		user.setVerificationCode(UUID.randomUUID().toString());
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
	@PreAuthorize("hasPermission(#user, 'edit')")
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
	 * Fetchs a user by email
	 * 
	 * @param email
	 * @return the decorated user object
	 */
	public U fetchUserByEmail(@Valid @Email @NotBlank String email) {
		
		log.debug("Fetching user by email: " + email);

		// fetch the user
		U user = userRepository.findByEmail(email)
			.orElseThrow(() -> MultiErrorException.of("email",
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

		U currentUser = LemonUtil.getUser();
		
		U user = userRepository.findOne(currentUser.getId());
		
		LemonUtil.check(user.getRoles().contains(Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	
		
		LemonUtil.check(verificationCode.equals(user.getVerificationCode()),
				"com.naturalprogrammer.spring.wrong.verificationCode").go();
		
		makeVerified(user);
		userRepository.save(user);
		
		LemonUtil.afterCommit(() -> {
			
			makeVerified(currentUser);
			currentUser.decorate(currentUser);
			log.debug("Removed UNVERIFIED role from current user.");		
		});
		
		log.debug("Verified user: " + user);		
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void forgotPassword(@Valid @Email @NotBlank String email) {
		
		log.debug("Processing forgot password for email: " + email);
		
		U user = userRepository.findByEmail(email)
				.orElseThrow(() -> MultiErrorException.of(
					"com.naturalprogrammer.spring.userNotFound"));

		user.setForgotPasswordCode(UUID.randomUUID().toString());
		userRepository.save(user);

		LemonUtil.afterCommit(() -> mailForgotPasswordLink(user));
	}
	
	protected void mailForgotPasswordLink(U user) {
		
		try {

			log.debug("Mailing forgot password link to user: " + user);

			String forgotPasswordLink =	properties.getApplicationUrl()
				    + "/users/" + user.getForgotPasswordCode()
					+ "/reset-password";
			
			mailSender.send(user.getEmail(),
					LemonUtil.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
					LemonUtil.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail",
						forgotPasswordLink));
			
			log.debug("Forgot password link mail queued.");
			
		} catch (MessagingException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}

	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void resetPassword(@Valid @NotBlank String forgotPasswordCode,
							  @Valid @Password String newPassword) {
		
		log.debug("Resetting password ...");

		U user = userRepository
			.findByForgotPasswordCode(forgotPasswordCode)
			.orElseThrow(() -> MultiErrorException.of(
				"com.naturalprogrammer.spring.invalidLink"));
		
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setForgotPasswordCode(null);
		
		userRepository.save(user);
		
		log.debug("Password reset.");	
		
	}

	@PreAuthorize("hasPermission(#user, 'edit')")
	@Validated(AbstractUser.UpdateValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void updateUser(U user, @Valid U updatedUser) {
		
		log.debug("Updating user: " + user);

		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.validateVersion(user, updatedUser);

		updateUserFields(user, updatedUser, LemonUtil.getUser());
		userRepository.save(user);
		
		log.debug("Updated user: " + user);		
	}
	
	@PreAuthorize("hasPermission(#user, 'edit')")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password for user: " + user);

		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.check("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		userRepository.save(user);
		
		LemonUtil.afterCommit(() -> {

			U currentUser = LemonUtil.getUser();
			
			if (currentUser.equals(user)) {
				
				log.debug("Logging out ...");
				LemonUtil.logOut();
				//currentUser.setPassword(user.getPassword());				
			}
		});
		
		log.debug("Changed password for user: " + user);
	}


	/**
	 * Override this if you have more fields
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

	
	public U userForClient() {
		return userForClient(LemonUtil.getUser());
	}

	
	/**
	 * Override this if you have more fields
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
	
	@PreAuthorize("hasPermission(#user, 'edit')")
	@Validated(AbstractUser.ChangeEmailValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void requestEmailChange(U user, @Valid U updatedUser) {
		
		log.debug("Requesting email change: " + user);

		LemonUtil.check("id", user != null,
				"com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.check("updatedUser.password",
			passwordEncoder.matches(updatedUser.getPassword(),
									LemonUtil.getUser().getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();

		user.setNewEmail(updatedUser.getNewEmail());
		user.setChangeEmailCode(UUID.randomUUID().toString());
		userRepository.save(user);
		
		LemonUtil.afterCommit(() -> mailChangeEmailLink(user));
		
		log.debug("Requested email change: " + user);		
	}

	protected void mailChangeEmailLink(U user) {
		
		try {
			
			log.debug("Mailing change email link to user: " + user);

			String changeEmailLink = properties.getApplicationUrl()
				    + "/users/" + user.getChangeEmailCode()
					+ "/change-email";
			
			mailSender.send(user.getEmail(),
				LemonUtil.getMessage(
					"com.naturalprogrammer.spring.changeEmailSubject"),
				LemonUtil.getMessage(
					"com.naturalprogrammer.spring.changeEmailEmail",
					 changeEmailLink));
			
			log.debug("Change email link mail queued.");
			
		} catch (MessagingException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}


	@PreAuthorize("isAuthenticated()")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changeEmail(@Valid @NotBlank String changeEmailCode) {
		
		log.debug("Changing email of current user ...");

		U currentUser = LemonUtil.getUser();
		
		U user = userRepository.findOne(currentUser.getId());
		
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
		
		LemonUtil.afterCommit(LemonUtil::logOut);
		
		log.debug("Changed email of user: " + user);		
	}
	
}
