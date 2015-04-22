package com.naturalprogrammer.spring.boot.security;

import java.util.Set;

import com.naturalprogrammer.spring.boot.user.BaseUser.Role;

public class UserData {
	
	private long id;
	private String name;
	Set<Role> roles;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<Role> getRoles() {
		return roles;
	}
	
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

}
