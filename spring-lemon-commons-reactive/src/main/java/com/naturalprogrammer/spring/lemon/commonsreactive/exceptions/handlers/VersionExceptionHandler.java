package com.naturalprogrammer.spring.lemon.commonsreactive.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class VersionExceptionHandler extends AbstractExceptionHandler<VersionException> {

	public VersionExceptionHandler() {
		
		super(VersionException.class);
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(VersionException ex) {
		return HttpStatus.CONFLICT;
	}
}