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

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser.SignUpValidation;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.Password;

@Validated
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public abstract class LemonService
	<U extends AbstractUser<U,ID>, ID extends Serializable> {

    private static final Log log = LogFactory.getLog(LemonService.class);
    
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
    public void afterContextRefreshed(ContextRefreshedEvent event) {
    	
    	log.info("Starting up Spring Lemon ...");
    	onStartup();
    	log.info("Spring Lemon started");
    	
    }
    
	/**
	 * Creates a new ADMIN user, if not found.
	 * Override if needed.
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public void onStartup() {
    	
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
	 * Returns the context data to be sent to the client,
	 * i.e. <em>reCaptchaSiteKey</em> and all the properties
	 * prefixed with <em>lemon.shared</em>.
	 * 
	 * To send custom properties, put those in your application
	 * properties in the format <em>lemon.shared.fooBar</em>.
	 * 
	 * You can also override this method.
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
		
		LemonUtil.afterCommit(() -> {
		
			LemonUtil.logInUser(user);
			sendVerificationMail(user);
			log.debug("Signed up user: " + user);
		});
	}
	
	protected U initUser(U user) {
		
		log.debug("Initializing user: " + user);

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		makeUnverified(user);
		
		return user;
	}
	
	protected void makeUnverified(U user) {
		user.getRoles().add(Role.UNVERIFIED);
		user.setVerificationCode(UUID.randomUUID().toString());
	}
	
	protected void makeVerified(U user) {
		user.getRoles().remove(Role.UNVERIFIED);
		user.setVerificationCode(null);
	}
	
	protected void sendVerificationMail(final U user) {
		try {
			
			log.debug("Sending verification mail to: " + user);

			String verifyLink = properties.getApplicationUrl()
				+ "/users/" + user.getVerificationCode() + "/verify";
			
			mailSender.send(user.getEmail(),
				LemonUtil.getMessage("com.naturalprogrammer.spring.verifySubject"),
				LemonUtil.getMessage(
					"com.naturalprogrammer.spring.verifyEmail",	verifyLink));
			
			log.debug("Verification mail to " + user.getEmail() + " queued.");
			
		} catch (MessagingException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}	
	
	/**
	 * Resends verification mail to user
	 * See here for details
	 * @param user
	 */
	@PreAuthorize("hasPermission(#user, 'edit')")
	public void resendVerificationMail(U user) {

		LemonUtil.check("id", user != null,
				"com.naturalprogrammer.spring.userNotFound").go();
		
		LemonUtil.check(user.getRoles().contains(Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	

		sendVerificationMail(user);
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

		LemonUtil.check("id", user != null,
			"com.naturalprogrammer.spring.userNotFound").go();
		
		user.decorate().hideConfidentialFields();
		
		return user;
	}
	
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
			
			currentUser.getRoles().remove(Role.UNVERIFIED);
			currentUser.decorate(currentUser);
			log.debug("Removed UNVERIFIED role from current user.");		
		});
		
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

		LemonUtil.check("id", user != null, "com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.validateVersion(user, updatedUser);

		U currentUser = LemonUtil.getUser();

		updateUserFields(user, updatedUser, currentUser);
		
		userRepository.save(user);
		
		log.debug("Updated user: " + user);		
	}
	
	@PreAuthorize("hasPermission(#user, 'edit')")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		log.debug("Changing password for user: " + user);

		LemonUtil.check("id", user != null, "com.naturalprogrammer.spring.userNotFound").go();
		LemonUtil.check("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(), user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		userRepository.save(user);
		
		LemonUtil.afterCommit(() -> {

			U currentUser = LemonUtil.getUser();
			
			if (currentUser.equals(user)) {
				
				log.debug("Setting password for logged in user.");
				currentUser.setPassword(user.getPassword());				
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

		if (user.isRolesEditable()) {
			
			log.debug("Updating roles for user: " + user);

			Set<String> roles = user.getRoles();
			
			if (updatedUser.isUnverified()) {
				
				if (!user.hasRole(Role.UNVERIFIED)) {
					makeUnverified(user); // make user unverified
					LemonUtil.afterCommit(() -> {
						sendVerificationMail(user); // send a verification mail to the user
					});
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
		
// This is not needed, because the logged in user can't update his own role
//		LemonUtil.afterCommit(() -> {
//			if (currentUser.equals(user)) {
//				
//				log.debug("Setting roles for logged in user.");
//				currentUser.setRoles(user.getRoles());
//			}
//		});

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
		user.setEmail(currentUser.getEmail());
		user.setRoles(currentUser.getRoles());
		user.decorate(currentUser);
		
		log.debug("Returning user for client: " + user);
		
		return user;
	}
	
}
