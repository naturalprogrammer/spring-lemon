package com.naturalprogrammer.spring.lemon.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A mock mail sender for 
 * writing the mails to the log.
 * 
 * @author Sanjay Patel
 */
public class MockMailSender implements MailSender {
	
	private static final Log log = LogFactory.getLog(MockMailSender.class);

	@Override
	public void send(String to, String subject, String body) {
		log.info("Sending mail to " + to);
		log.info("Subject: " + subject);
		log.info("Body: " + body);
	}

}
