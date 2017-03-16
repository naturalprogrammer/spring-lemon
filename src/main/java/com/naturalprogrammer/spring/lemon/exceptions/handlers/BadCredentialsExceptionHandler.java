package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BadCredentialsExceptionHandler extends AbstractExceptionHandler<BadCredentialsException> {
	
	private static final Log log = LogFactory.getLog(BadCredentialsExceptionHandler.class);

	public BadCredentialsExceptionHandler() {
		
		super(BadCredentialsException.class.getSimpleName());
		log.info("Created");
	}
	
	@Override
	protected HttpStatus getStatus(BadCredentialsException ex) {
		return HttpStatus.UNAUTHORIZED;
	}
}
