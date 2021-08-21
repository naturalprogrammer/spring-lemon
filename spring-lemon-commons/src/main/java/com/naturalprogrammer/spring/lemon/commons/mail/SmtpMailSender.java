/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.spring.lemon.commons.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


/**
 * An SMTP mail sender, which sends mails
 * using an injected JavaMailSender.
 * 
 * @author Sanjay Patel
 *
 */
public class SmtpMailSender implements MailSender<LemonMailData> {
	
	private static final Log log = LogFactory.getLog(SmtpMailSender.class);

	private final JavaMailSender javaMailSender;
	
	public SmtpMailSender(JavaMailSender javaMailSender) {
		
		this.javaMailSender = javaMailSender;
		log.info("Created");
	}


	/**
	 * Sends a mail using a MimeMessageHelper
	 */
	@Override
	@Async
	public void send(LemonMailData mail) {
		
		log.info("Sending SMTP mail from thread " + Thread.currentThread().getName()); // toString gives more info    	

		// create a mime-message
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper;

		try {
			
			// create a helper
			helper = new MimeMessageHelper(message, true);
			// set the attributes
			helper.setSubject(mail.getSubject());
			helper.setTo(mail.getTo());
			helper.setText(mail.getBody(), true); // true indicates html
			// continue using helper object for more functionalities like adding attachments, etc.  
			
		} catch (MessagingException e) {

			throw new RuntimeException(e);
		}
		
		//send the mail
		javaMailSender.send(message);		
		log.info("Sent SMTP mail from thread " + Thread.currentThread().getName());    	
	}

}
