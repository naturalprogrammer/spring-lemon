package com.naturalprogrammer.spring.lemon.commons.mail;

import lombok.Getter;
import lombok.Setter;

/**
 * Data needed for sending a mail.
 * Override this if you need more data to be sent.
 */
@Getter @Setter
public class LemonMailData {
	
	private String to;
	private String subject;
	private String body;

	public static LemonMailData of(String to, String subject, String body) {
		
		LemonMailData data = new LemonMailData();
		
		data.to = to;
		data.subject = subject;
		data.body = body;

		return data;
	}
}
