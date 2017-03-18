package com.naturalprogrammer.spring.lemon.security.principalextractors;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;

public class DefaultPrincipalExtractor<U extends AbstractUser<U,?>> extends AbstractPrincipalExtractor<U> {
	
	public DefaultPrincipalExtractor() {
		
		super(LemonPrincipalExtractor.DEFAULT);
		log.info("Created");
	}
}
