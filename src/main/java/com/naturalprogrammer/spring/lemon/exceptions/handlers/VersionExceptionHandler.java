package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.exceptions.VersionException;

@Component
public class VersionExceptionHandler extends AbstractExceptionHandler<VersionException> {

	public VersionExceptionHandler() {
		
		super(VersionException.class.getSimpleName());
	}
	
	@Override
	protected HttpStatus getStatus(VersionException ex) {
		return HttpStatus.CONFLICT;
	}
}
