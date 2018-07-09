package com.naturalprogrammer.spring.lemon.commons.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * A lighter User class,
 * mainly used for holding logged-in user data 
 */
@Getter @Setter
public class UserDto<ID extends Serializable> {

	private ID id;
	private String username;
	private String password;
	private Set<String> roles = new HashSet<String>();
	private Serializable tag;
	
	private boolean unverified = false;
	private boolean blocked = false;
	private boolean admin = false;
	private boolean goodUser = false;
	private boolean goodAdmin = false;
}
