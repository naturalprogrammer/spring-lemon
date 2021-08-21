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
