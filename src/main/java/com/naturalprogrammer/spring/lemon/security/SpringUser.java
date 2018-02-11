package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SpringUser<ID extends Serializable> {

	private ID id;
	private String username;
	private String password;
	private Set<String> roles = new HashSet<String>();
	private Serializable tag;
	private String nonce;
	
	private boolean unverified = false;
	private boolean blocked = false;
	private boolean admin = false;
	private boolean goodUser = false;
	private boolean goodAdmin = false;
	
	public ID getId() {
		return id;
	}
	public void setId(ID id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Serializable getTag() {
		return tag;
	}
	public void setTag(Serializable tag) {
		this.tag = tag;
	}
	public boolean isUnverified() {
		return unverified;
	}
	public void setUnverified(boolean unverified) {
		this.unverified = unverified;
	}
	public boolean isBlocked() {
		return blocked;
	}
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public boolean isGoodUser() {
		return goodUser;
	}
	public void setGoodUser(boolean goodUser) {
		this.goodUser = goodUser;
	}
	public boolean isGoodAdmin() {
		return goodAdmin;
	}
	public void setGoodAdmin(boolean goodAdmin) {
		this.goodAdmin = goodAdmin;
	}
	public Set<String> getRoles() {
		return roles;
	}
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
}
