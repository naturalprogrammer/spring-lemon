package com.naturalprogrammer.spring.lemon.forms;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class NonceForm<ID> {
	
	@NotNull
	private ID userId;
	
	@NotBlank
	private String nonce;
	
	private Long expirationMilli;
	
	public ID getUserId() {
		return userId;
	}
	public void setUserId(ID userId) {
		this.userId = userId;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public Long getExpirationMilli() {
		return expirationMilli;
	}
	public void setExpirationMilli(Long expirationMilli) {
		this.expirationMilli = expirationMilli;
	}
}
