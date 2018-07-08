package com.naturalprogrammer.spring.lemon.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A mock mail sender for 
 * writing the mails to the log.
 * 
 * @author Sanjay Patel
 */
public class MockMailSender implements MailSender<LemonMailData> {
	
	private static final Log log = LogFactory.getLog(MockMailSender.class);
	
	public MockMailSender() {
		log.info("Created");
	}

	@Override
	public void send(LemonMailData mail) {
		
		log.info("Sending mail to " + mail.getTo());
		log.info("Subject: " + mail.getSubject());
		log.info("Body: " + mail.getBody());
	}

}
