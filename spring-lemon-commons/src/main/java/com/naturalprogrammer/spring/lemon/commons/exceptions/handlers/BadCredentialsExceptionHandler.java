package com.naturalprogrammer.spring.lemon.commons.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BadCredentialsExceptionHandler extends AbstractExceptionHandler<BadCredentialsException> {
	
	public BadCredentialsExceptionHandler() {
		
		super(BadCredentialsException.class);
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(BadCredentialsException ex) {
		return HttpStatus.UNAUTHORIZED;
	}
}
