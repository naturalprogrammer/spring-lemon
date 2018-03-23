package com.naturalprogrammer.spring.lemon.mail;

public class LemonMailData {
	
	private String to;
	private String subject;
	private String body;
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public static LemonMailData of(String to, String subject, String body) {
		
		LemonMailData data = new LemonMailData();
		
		data.to = to;
		data.subject = subject;
		data.body = body;

		return data;
	}
}
