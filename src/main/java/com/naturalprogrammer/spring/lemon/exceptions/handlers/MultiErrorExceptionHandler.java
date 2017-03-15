package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

@Component
public class MultiErrorExceptionHandler extends AbstractExceptionHandler<MultiErrorException> {

	public MultiErrorExceptionHandler() {
		
		super(MultiErrorException.class.getSimpleName());
	}
	
	@Override
	protected HttpStatus getStatus(MultiErrorException ex) {
		return HttpStatus.UNPROCESSABLE_ENTITY;
	}
	
	@Override
	protected Collection<FieldError> getErrors(MultiErrorException ex) {
		return ex.getErrors();
	}
}
