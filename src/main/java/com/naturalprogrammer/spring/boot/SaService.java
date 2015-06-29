package com.naturalprogrammer.spring.boot;

import java.io.Serializable;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.boot.domain.AbstractUser;
import com.naturalprogrammer.spring.boot.domain.AbstractUser.Role;
import com.naturalprogrammer.spring.boot.domain.AbstractUser.SignUpValidation;
import com.naturalprogrammer.spring.boot.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.boot.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.boot.mail.MailSender;
import com.naturalprogrammer.spring.boot.util.SaUtil;
import com.naturalprogrammer.spring.boot.validation.Password;

@Validated
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public abstract class SaService<U extends AbstractUser<U,ID>, ID extends Serializable> {

    private final Log log = LogFactory.getLog(getClass());
    
	@Value("${adminUser.email}")
	private String adminEmail;

	@Value("${adminUser.password}")
	private String adminPassword;
	
	@Autowired
	private PublicProperties properties;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
    private MailSender mailSender;

    @Autowired
	private AbstractUserRepository<U, ID> userRepository;
    
    
    /**
     * This method needs to be public; otherwise Spring screams
     * 
     * @param event
     */
    @EventListener
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public void afterContextRefreshed(ContextRefreshedEvent event) {
    	
    	onStartup();
    	
    }
    
	/**
	 * Override this if needed
	 */
    protected void onStartup() {
		U user = userRepository.findByEmail(adminEmail);
		if (user == null) {
	    	user = createAdminUser();
	    	userRepository.save(user);
		}
	}

	
	/**
	 * Override this if you have more fields to set
	 */
    protected U createAdminUser() {
		
		U user = newUser();
		
		user.setEmail(adminEmail);
		user.setPassword(passwordEncoder.encode(adminPassword));
		user.getRoles().add(Role.ADMIN);
		
		return user;

	}

	abstract protected U newUser();

	@Autowired
	private PublicProperties publicProperties;
	
	/**
	 * To send custom properties, you can 
	 * use other.foo in application.properties OR
	 * Override PublicProperties class and this method
	 * 
	 * @return
	 */
	public PublicProperties getContext() {
		
		return publicProperties;
		
		
//		ContextDto contextDto = new ContextDto();
//		contextDto.setReCaptchaSiteKey(reCaptchaSiteKey);
//		return contextDto;		
	}
	
	@PreAuthorize("isAnonymous()")
	@Validated(SignUpValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid U user) {
		
		initUser(user);
		userRepository.save(user);
		sendVerificationMail(user);
		SaUtil.logInUser(user);
		
	}
	
	protected U initUser(U user) {
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.getRoles().add(Role.UNVERIFIED);
		user.setVerificationCode(UUID.randomUUID().toString());
		
		return user;
		
	}
	
	protected void sendVerificationMail(final U user) {
        SaUtil.afterCommit(() -> {
    		try {
    			String verifyLink = properties.getApplicationUrl() + "/users/" + user.getVerificationCode() + "/verify";
    			mailSender.send(user.getEmail(), SaUtil.getMessage("verifySubject"), SaUtil.getMessage("verifyEmail", verifyLink));
    			log.info("Verification mail to " + user.getEmail() + " queued.");
			} catch (MessagingException e) {
				log.error(ExceptionUtils.getStackTrace(e));
			}
        });			
	}

	public U fetchUser(@Valid @Email @NotBlank String email) {
		
		U user = userRepository.findByEmail(email);
		
		SaUtil.check("email", user != null, "userNotFound").go();
		
		user.decorate().hideConfidentialFields();

		return user;
	}

	
	public U fetchUser(U user) {
		
		SaUtil.check(user != null, "userNotFound").go();

		user.decorate().hideConfidentialFields();
		
		return user;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void verifyUser(String verificationCode) {
		
		U user = userRepository.findByVerificationCode(verificationCode);
		SaUtil.check(user != null, "userNotFound").go();
		
		user.setVerificationCode(null);
		user.getRoles().remove(Role.UNVERIFIED);
		userRepository.save(user);
		
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void forgotPassword(@Valid @Email @NotBlank String email) {
		
		final U user = userRepository.findByEmail(email);

		SaUtil.check(user != null, "userNotFound").go();
		
		user.setForgotPasswordCode(UUID.randomUUID().toString());
		userRepository.save(user);

		SaUtil.afterCommit(() -> {
		    mailForgotPasswordLink(user);
		});
		
//		TransactionSynchronizationManager.registerSynchronization(
//			    new TransactionSynchronizationAdapter() {
//			        @Override
//			        public void afterCommit() {
//			        	try {
//							mailForgotPasswordLink(user);
//						} catch (MessagingException e) {
//							log.error(ExceptionUtils.getStackTrace(e));
//						}
//			        }
//
//		    });				
	}
	
	private void mailForgotPasswordLink(U user) {
		
		try {
			String forgotPasswordLink = 
					properties.getApplicationUrl() + "/reset-password/" +
					user.getForgotPasswordCode();
			mailSender.send(user.getEmail(),
					SaUtil.getMessage("forgotPasswordSubject"),
					SaUtil.getMessage("forgotPasswordEmail", forgotPasswordLink));
		} catch (MessagingException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}

	}

	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void resetPassword(String forgotPasswordCode, @Valid @Password String newPassword) {
		
		U user = userRepository.findByForgotPasswordCode(forgotPasswordCode);
		SaUtil.check(user != null, "invalidLink").go();
		
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setForgotPasswordCode(null);
		
		userRepository.save(user);
		
	}

	@PreAuthorize("hasPermission(#user, 'edit')")
	@Validated(AbstractUser.UpdateValidation.class)
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void updateUser(U user, @Valid U updatedUser) {
		
		SaUtil.check(user != null, "userNotFound").go();
		SaUtil.validateVersion(user, updatedUser);

		U loggedIn = SaUtil.getLoggedInUser();

		updateUserFields(user, updatedUser, loggedIn);
		
		userRepository.save(user);		
		
	}
	
	@PreAuthorize("hasPermission(#user, 'edit')")
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void changePassword(U user, @Valid ChangePasswordForm changePasswordForm) {
		
		SaUtil.check(user != null, "userNotFound").go();
		SaUtil.check("oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(), user.getPassword()),
			"wrong.password").go();
		
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		userRepository.save(user);
		
		SaUtil.afterCommit(() -> {
			U loggedIn = SaUtil.getLoggedInUser();
			if (loggedIn.equals(user))
				loggedIn.setPassword(user.getPassword());
		});
		
	}


	/**
	 * Override this if you have more fields
	 * 
	 * @param user
	 * @param updatedUser
	 * @param loggedIn
	 */
	protected void updateUserFields(U user, U updatedUser, U loggedIn) {

		if (user.isRolesEditable()) {
			
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
		
		SaUtil.afterCommit(() -> {
			if (loggedIn.equals(user))
				loggedIn.setRoles(user.getRoles());
		});

	}

	public U userForClient() {

		return userForClient(SaUtil.getLoggedInUser());
		
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
		return user.decorate();
	}
	
}
