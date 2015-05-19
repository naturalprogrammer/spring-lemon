package com.naturalprogrammer.spring.boot;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.boot.SaUser.Role;
import com.naturalprogrammer.spring.boot.mail.MailSender;
import com.naturalprogrammer.spring.boot.security.UserDetailsImpl;

@Validated
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public abstract class SaService<U extends SaUser, S extends SignupForm> {

    private final Log log = LogFactory.getLog(getClass());
    
	@Value(SaUtil.APPLICATION_URL)
    private String applicationUrl;
	
	@Autowired
    private PasswordEncoder passwordEncoder;

	@Autowired
    private MailSender mailSender;

    @Autowired
	private SaUserRepository<U> userRepository;
    
	public ContextDto getContext() {
		ContextDto contextDto = new ContextDto();
		contextDto.setUserDto(SaUtil.getUserDto());
		return contextDto;		
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void signup(@Valid S signupForm) {
		
		U user = createUser(signupForm);
		userRepository.save(user);
		sendVerificationMail(user);
		
	}
	
	protected U createUser(S signupForm) {
		
		final U user = (U) SaUtil.getBean(SaUser.class);
		
		user.setEmail(signupForm.getEmail());
		user.setName(signupForm.getName());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.getRoles().add(Role.UNVERIFIED);
		user.setVerificationCode(RandomStringUtils.randomAlphanumeric(16));
		
		return user;
		
	}
	
	protected void sendVerificationMail(final U user) {
		TransactionSynchronizationManager.registerSynchronization(
			    new TransactionSynchronizationAdapter() {
			        @Override
			        public void afterCommit() {
			    		try {
			    			String verifyLink = applicationUrl + "/users/" + user.getVerificationCode() + "/verify";
			    			mailSender.send(user.getEmail(), SaUtil.getMessage("verifySubject"), SaUtil.getMessage("verifyEmail", verifyLink));
			    			log.info("Verification mail to " + user.getEmail() + " queued.");
						} catch (MessagingException e) {
							log.error(ExceptionUtils.getStackTrace(e));
						}
			        }
			    });
			
	}

}
