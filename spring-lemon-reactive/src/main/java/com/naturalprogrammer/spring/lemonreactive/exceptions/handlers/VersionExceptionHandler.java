package com.naturalprogrammer.spring.lemonreactive.exceptions.handlers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class VersionExceptionHandler extends AbstractExceptionHandler<VersionException> {

	public VersionExceptionHandler() {
		
		super(VersionException.class.getSimpleName());
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(VersionException ex) {
		return HttpStatus.CONFLICT;
	}
}