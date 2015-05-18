package com.naturalprogrammer.spring.boot.user;

import javax.mail.MessagingException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.naturalprogrammer.spring.boot.Sa;
import com.naturalprogrammer.spring.boot.entities.User;
import com.naturalprogrammer.spring.boot.mail.MailSender;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class UserService<U extends User> {
	
	private final Log log = LogFactory.getLog(getClass());

	@Value(Sa.APPLICATION_URL)
    private String applicationUrl;
	
    @Autowired
    private MailSender mailSender;

    @Autowired
	private BaseUserRepository<U> userRepository;

	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findByForgotPasswordCode(String forgotPasswordCode) {
		return userRepository.findByForgotPasswordCode(forgotPasswordCode);
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void save(U baseUser) {
		userRepository.save(baseUser);
	}
	
	public void sendVerificationMail(final U user) {
		TransactionSynchronizationManager.registerSynchronization(
			    new TransactionSynchronizationAdapter() {
			        @Override
			        public void afterCommit() {
			    		try {
			    			String verifyLink = applicationUrl + "/users/" + user.getVerificationCode() + "/verify";
			    			mailSender.send(user.getEmail(), Sa.getMessage("verifySubject"), Sa.getMessage("verifyEmail", verifyLink));
			    			log.info("Verification mail to " + user.getEmail() + " queued.");
						} catch (MessagingException e) {
							log.error(ExceptionUtils.getStackTrace(e));
						}
			        }
			    });
			
	}
	
	
}
