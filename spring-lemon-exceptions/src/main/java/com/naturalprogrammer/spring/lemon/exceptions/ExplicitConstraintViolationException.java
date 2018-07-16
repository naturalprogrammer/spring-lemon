package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import lombok.Getter;

public class ExplicitConstraintViolationException extends ConstraintViolationException {

	private static final long serialVersionUID = 3723548255231135762L;

	@Getter
	private final String objectName;
	
	public ExplicitConstraintViolationException(Set<? extends ConstraintViolation<?>> constraintViolations, String objectName) {
		super(constraintViolations);
		this.objectName = objectName;
	}	
}
