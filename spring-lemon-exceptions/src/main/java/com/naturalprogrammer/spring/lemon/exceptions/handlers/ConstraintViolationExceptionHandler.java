package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import javax.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConstraintViolationExceptionHandler extends AbstractValidationExceptionHandler<ConstraintViolationException> {

	public ConstraintViolationExceptionHandler() {
		
		super(ConstraintViolationException.class);
		log.info("Created");
	}
	
	@Override
	public Collection<LemonFieldError> getErrors(ConstraintViolationException ex) {
		return LemonFieldError.getErrors(ex.getConstraintViolations());
	}

}
