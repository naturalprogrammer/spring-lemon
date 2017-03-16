package com.naturalprogrammer.spring.lemon.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;

public interface LemonPrincipalExtractor extends PrincipalExtractor {
	
	String getProvider();
}
