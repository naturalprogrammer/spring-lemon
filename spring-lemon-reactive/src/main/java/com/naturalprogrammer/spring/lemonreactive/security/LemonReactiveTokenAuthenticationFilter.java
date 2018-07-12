package com.naturalprogrammer.spring.lemonreactive.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

public class LemonReactiveTokenAuthenticationFilter extends AuthenticationWebFilter {

	public LemonReactiveTokenAuthenticationFilter(ReactiveAuthenticationManager authenticationManager) {
		super(authenticationManager);
	}
}
