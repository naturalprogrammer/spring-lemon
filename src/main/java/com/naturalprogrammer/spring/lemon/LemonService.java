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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.SignUpValidation;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.Password;

@Validated
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public abstract class LemonService<U extends AbstractUser<U,ID>, ID extends Serializable> {

    private final Log log = LogFactory.getLog(getClass());
    
	@Autowired
	private LemonProperties properties;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
    private MailSender mailSender;

    @Autowired
	private AbstractUserRepository<U, ID> userRepository;
    
    @Autowired
	private UserDetailsService userDetailsService;   
    
    /**
     * This method needs to be public; otherwise Spring screams
     * 
     * @param event
     */
    @EventListener
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public void afterContextRefreshed(ContextRefreshedEvent event) {
    	
    	log.info("Starting up Spring Lemon ...");
    	onStartup();
    	log.info("Spring Lemon started");
    	
    }
    
	/**
	 * Creates a new ADMIN user, if not found.
	 * Override if needed.
	 */
    protected void onStartup() {
    	
		try {
			userDetailsService
				.loadUserByUsername(properties.getAdmin().getLogin());
		} catch (UsernameNotFoundException e) {
	    	U user = createAdminUser();
	    	userRepository.save(user);			
		}
	}

	
	/**
	 * Override this if you have more fields to set,
	 * or if the email field if not your loginId
	 */
    protected U createAdminUser() {
		
    	String adminEmail = properties.getAdmin().getLogin();
    	
    	log.info("Creating the first admin user: " + adminEmail);

    	U user = newUser();
		user.setEmail(adminEmail);
		user.setPassword(passwordEncoder.encode(
			properties.getAdmin().getPassword()));
		user.getRoles().add(Role.ADMIN);
		
		return user;

	}

	abstract protected U newUser();

	@Autowired
	private LemonProperties lemonProperties;
	
	/**
	 * To send custom properties, you can 
	 * use other.foo in application.properties OR
	 * Override LemonProperties class and this method
	 * 
	 * @return
	 */
	public Map<String, Object> getContext() {
		
		Map<String, Object> context = new HashMap<String, Object>(2);
		context.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
		context.put("shared", properties.getShared());
		
		log.debug("Returning context: " + context);

		return context;		
	}
	
	@PreAuthorize("isAnonymous()")
	@Validated(SignUpValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid U user) {
		
		log.debug("Signing up user: " + user);

		initUser(user);
		userRepository.save(user);
		sendVerificationMail(user);
		LemonUtil.logInUser(user);
		
		log.debug("Signed up user: " + user);
		
		
	}
	
	protected U initUser(U user) {
		
		log.debug("Initializing user: " + user);

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.getRoles().add(Role.UNVERIFIED);
		user.setVerificationCode(UUID.randomUUID().toString());
		
		return user;
		
	}
	
	protected void sendVerificationMail(final U user) {
        LemonUtil.afterCommit(() -> {
    		try {
    			
    			log.debug("Sending verification mail to: " + user);

    			String verifyLink = properties.getApplicationUrl() + "/users/" + user.getVerificationCode() + "/verify";
    			mailSender.send(user.getEmail(),
    					LemonUtil.getMessage("com.naturalprogrammer.spring.verifySubject"),
    					LemonUtil.getMessage("com.naturalprogrammer.spring.verifyEmail", verifyLink));
    			
    			log.debug("Verification mail to " + user.getEmail() + " queued.");
    			
			} catch (MessagingException e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
        });			
	}

	public U fetchUser(@Valid @Email @NotBlank String email) {
		
		log.debug("Fetching user by email: " + email);

		U user = userRepository.findByEmail(email);
		LemonUtil.check("email", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		user.decorate().hideConfidentialFields();
		
		log.debug("Returning user: " + user);		

		return user;
	}

	
	public U fetchUser(U user) {
		
		log.debug("Fetching user: " + user);

		LemonUtil.check(user != null, "com.naturalprogrammer.spring.userNotFound").go();
		user.decorate().hideConfidentialFields();
		
		return user;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void verifyUser(String verificationCode) {
		
		log.debug("Verifying user ...");

		U user = userRepository.findByVerificationCode(verificationCode);
		LemonUtil.check(user != null, "com.naturalprogrammer.spring.userNotFound").go();
		
		user.setVerificationCode(null);
		user.getRoles().remove(Role.UNVERIFIED);
		userRepository.save(user);
		
		log.debug("Verified user: " + user);		
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void forgotPassword(@Valid @Email @NotBlank String email) {
		
		log.debug("Processing forgot password for email: " + email);
		final U user = userRepository.findByEmail(email);

		LemonUtil.check(user != null, "com.naturalprogrammer.spring.userNotFound").go();
		
		user.setForgotPasswordCode(UUID.randomUUID().toString());
		userRepository.save(user);

		LemonUtil.afterCommit(() -> {
		    mailForgotPasswordLink(user);
		});
		
	}
	
	private void mailForgotPasswordLink(U user) {
		
		try {
			
			log.debug("Mailing forgot password link to user: " + user);

			String forgotPasswordLink = 
					properties.getApplicationUrl() + "/reset-password/" +
					user.getForgotPasswordCode();
			mailSender.send(user.getEmail(),
					LemonUtil.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
					LemonUtil.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail", forgotPasswordLink));
			
			log.debug("Forgot password link mail queued.");
			
		} catch (MessagingException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

	}

	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void resetPassword(String forgotPasswordCode, @Valid @Password String newPassword) {
		
		log.debug("Resetting password ...");

		U user = userRepository.findByForgotPasswordCode(forgotPasswordCode);
		LemonUtil.check(user != null, "com.naturalprogrammer.spring.invalidLink").go();
		
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

		LemonUtil.check(user != null, "com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.validateVersion(user, updatedUser);

		U loggedIn = LemonUtil.getUser();

		updateUserFields(user, updatedUser, loggedIn);
		
		userRepository.save(user);
		
		log.debug("Updated user: " + user);		
	}
	
	@PreAuthorize("hasPermission(#user, 'edit')")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password for user: " + user);

		LemonUtil.check(user != null, "com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.check("oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(), user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		userRepository.save(user);
		
		LemonUtil.afterCommit(() -> {

			U loggedIn = LemonUtil.getUser();
			
			if (loggedIn.equals(user)) {
				
				log.debug("Setting password for logged in user.");
				loggedIn.setPassword(user.getPassword());				
			}
		});
		
		log.debug("Changed password for user: " + user);
		
	}


	/**
	 * Override this if you have more fields
	 * 
	 * @param user
	 * @param updatedUser
	 * @param loggedIn
	 */
	protected void updateUserFields(U user, U updatedUser, U loggedIn) {

		log.debug("Updating user fields for user: " + user);

		if (user.isRolesEditable()) {
			
			log.debug("Updating roles for user: " + user);

			Set<String> roles = user.getRoles();
			
			if (updatedUser.isUnverified())
				roles.add(Role.UNVERIFIED);
			else
				roles.remove(Role.UNVERIFIED);
			
			if (updatedUser.isAdmin())
				roles.add(Role.ADMIN);
			else
				roles.remove(Role.ADMIN);
			
			if (updatedUser.isBlocked())
				roles.add(Role.BLOCKED);
			else
				roles.remove(Role.BLOCKED);
		}
		
		LemonUtil.afterCommit(() -> {
			if (loggedIn.equals(user)) {
				
				log.debug("Setting roles for logged in user.");
				loggedIn.setRoles(user.getRoles());
			}
		});

	}

	public U userForClient() {

		return userForClient(LemonUtil.getUser());
		
	}

	
	/**
	 * Override this if you have more fields
	 * 
	 * @param loggedIn
	 */
	protected U userForClient(U loggedIn) {
		
		if (loggedIn == null)
			return null;
		
		U user = newUser();
		user.setIdForClient(loggedIn.getId());
		user.setEmail(loggedIn.getEmail());
		user.setRoles(loggedIn.getRoles());
		user.decorate(loggedIn);
		
		log.debug("Returning user for client: " + user);
		
		return user;

	}
	
}
