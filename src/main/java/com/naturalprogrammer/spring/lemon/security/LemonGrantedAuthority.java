package com.naturalprogrammer.spring.lemon.security;

import org.springframework.security.core.GrantedAuthority;

public class LemonGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = -1447095521180508501L;

    private String authority;

	public LemonGrantedAuthority() {}

    public LemonGrantedAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
		this.authority = authority;
	}
}
