package com.naturalprogrammer.spring.lemon.commons.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * A lighter User class,
 * mainly used for holding logged-in user data 
 */
@Getter @Setter
public class UserDto {

	private String id;
	private String username;
	private String password;
	private Set<String> roles = new HashSet<String>();
	private Serializable tag;
	
	private boolean unverified = false;
	private boolean blocked = false;
	private boolean admin = false;
	private boolean goodUser = false;
	private boolean goodAdmin = false;
	
	public void initialize() {
		
		unverified = roles.contains(UserUtils.Role.UNVERIFIED);
		blocked = roles.contains(UserUtils.Role.BLOCKED);
		admin = roles.contains(UserUtils.Role.ADMIN);
		goodUser = !(unverified || blocked);
		goodAdmin = goodUser && admin;
	}
}
