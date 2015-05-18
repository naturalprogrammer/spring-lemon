package com.naturalprogrammer.spring.boot.mail;

import javax.mail.MessagingException;

public interface MailSender {

	public abstract void send(String to, String subject, String body) throws MessagingException;

}