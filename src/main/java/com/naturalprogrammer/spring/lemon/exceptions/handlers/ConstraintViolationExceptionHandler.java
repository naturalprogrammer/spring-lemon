package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import javax.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConstraintViolationExceptionHandler extends AbstractExceptionHandler<ConstraintViolationException> {

	public ConstraintViolationExceptionHandler() {
		
		super(ConstraintViolationException.class.getSimpleName());
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(ConstraintViolationException ex) {
		return HttpStatus.UNPROCESSABLE_ENTITY;
	}
	
	@Override
	public Collection<FieldError> getErrors(ConstraintViolationException ex) {
		return FieldError.getErrors(ex.getConstraintViolations());
	}
	
	@Override
	public String getMessage(ConstraintViolationException ex) {
		return LemonUtils.getMessage("com.naturalprogrammer.spring.validationError");
	}
}
