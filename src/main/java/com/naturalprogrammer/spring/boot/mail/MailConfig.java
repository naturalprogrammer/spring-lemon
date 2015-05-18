package com.naturalprogrammer.spring.boot.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {
	
	@Bean
	@Profile("dev")
	public MailSender mockMailSender() {
		return new MockMailSender();
	}

	@Bean
	@Profile("!dev")
	public MailSender smtpMailSender(JavaMailSender javaMailSender) {
		SmtpMailSender mailSender = new SmtpMailSender();
		mailSender.setJavaMailSender(javaMailSender);
		return mailSender;
	}

}
