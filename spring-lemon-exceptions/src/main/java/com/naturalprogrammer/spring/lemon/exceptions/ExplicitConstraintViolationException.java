package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import lombok.Getter;

@Getter
public class ExplicitConstraintViolationException extends RuntimeException {

	private static final long serialVersionUID = 3723548255231135762L;

	// list of errors
	private List<LemonFieldError> errors = new ArrayList<>(10);

	public ExplicitConstraintViolationException addErrors(Set<? extends ConstraintViolation<?>> constraintViolations, String objectName) {
		
		errors.addAll(constraintViolations.stream()
				.map(constraintViolation ->
					new LemonFieldError(
							objectName + "." + constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessageTemplate(),
							constraintViolation.getMessage()))
			    .collect(Collectors.toList()));
		
		return this;
	}
	
	/**
	 * Throws the exception, if there are accumulated errors
	 */
	public void go() {
		if (!errors.isEmpty())
			throw this;
	}
}
