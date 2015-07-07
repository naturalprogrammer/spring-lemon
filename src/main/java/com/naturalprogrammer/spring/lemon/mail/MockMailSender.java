package com.naturalprogrammer.spring.lemon.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class MockMailSender implements MailSender {
	
	private static final Logger logger = LoggerFactory.getLogger(MockMailSender.class);

	/* (non-Javadoc)
	 * @see com.goodvideotutorials.spring.mail.MailSender#send(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void send(String to, String subject, String body) {
		logger.info("Sending mail to " + to);
		logger.info("Subject: " + subject);
		logger.info("Body: " + body);
	}

}
