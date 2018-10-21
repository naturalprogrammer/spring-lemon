package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Holds a field or form error
 * 
 * @author Sanjay Patel
 */
@Getter @AllArgsConstructor @ToString
public class LemonFieldError {
	
	// Name of the field. Null in case of a form level error. 
	private String field;
	
	// Error code. Typically the I18n message-code.
	private String code;
	
	// Error message
	private String message;

	/**
	 * Converts a set of ConstraintViolations
	 * to a list of FieldErrors
	 * 
	 * @param constraintViolations
	 */
	public static List<LemonFieldError> getErrors(
			Set<ConstraintViolation<?>> constraintViolations) {
		
		return constraintViolations.stream()
				.map(LemonFieldError::of).collect(Collectors.toList());	
	}
	

	/**
	 * Converts a ConstraintViolation
	 * to a FieldError
	 */
	private static LemonFieldError of(ConstraintViolation<?> constraintViolation) {
		
		// Get the field name by removing the first part of the propertyPath.
		// (The first part would be the service method name)
		String field = StringUtils.substringAfter(
				constraintViolation.getPropertyPath().toString(), ".");
		
		return new LemonFieldError(field,
				constraintViolation.getMessageTemplate(),
				constraintViolation.getMessage());		
	}

	public static List<LemonFieldError> getErrors(WebExchangeBindException ex) {
		
		List<LemonFieldError> errors = ex.getFieldErrors().stream()
			.map(LemonFieldError::of).collect(Collectors.toList());
		
		errors.addAll(ex.getGlobalErrors().stream()
			.map(LemonFieldError::of).collect(Collectors.toSet()));
		
		return errors;
	}

	private static LemonFieldError of(FieldError fieldError) {
		
		return new LemonFieldError(fieldError.getObjectName() + "." + fieldError.getField(),
				fieldError.getCode(), fieldError.getDefaultMessage());
	}

	public static LemonFieldError of(ObjectError error) {
		
		return new LemonFieldError(error.getObjectName(),
				error.getCode(), error.getDefaultMessage());
	}

}
