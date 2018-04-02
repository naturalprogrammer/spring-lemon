package com.naturalprogrammer.spring.lemon.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT Authentication token
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 7032335279756013130L;

	private UserDetails principal;
	private String jwtToken;

	public JwtAuthenticationToken(String token) {
		super(AuthorityUtils.NO_AUTHORITIES);
		this.jwtToken = token;
	}

	public JwtAuthenticationToken(UserDetails principal, String jwtToken, Collection<? extends GrantedAuthority> authorities) {
		
		super(authorities);
		this.principal = principal;
		this.jwtToken = jwtToken;		
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return jwtToken;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}
}