package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import javax.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConstraintViolationExceptionHandler<E extends ConstraintViolationException> extends AbstractExceptionHandler<E> {

	public ConstraintViolationExceptionHandler() {
		
		super(ConstraintViolationException.class.getSimpleName());
		log.info("Created");
	}
	
	public ConstraintViolationExceptionHandler(String exceptionName) {
		super(exceptionName);
	}

	@Override
	public HttpStatus getStatus(E ex) {
		return HttpStatus.UNPROCESSABLE_ENTITY;
	}
	
	@Override
	public Collection<LemonFieldError> getErrors(E ex) {
		return LemonFieldError.getErrors(ex.getConstraintViolations());
	}
	
	@Override
	public String getMessage(E ex) {
		return LexUtils.getMessage("com.naturalprogrammer.spring.validationError");
	}
}
