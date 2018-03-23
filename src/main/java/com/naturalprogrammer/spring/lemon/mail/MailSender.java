package com.naturalprogrammer.spring.lemon.mail;

/**
 * The mail sender interface for sending mail
 * 
 * @author Sanjay Patel
 */
public interface MailSender<MailData> {

	void send(MailData mail);
}