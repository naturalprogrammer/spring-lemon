package com.naturalprogrammer.spring.lemon.validation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;

/**
 * Holds a field or form error
 * 
 * @author Sanjay Patel
 */
public class FieldError {
	
	// Name of the field. Null in case of a form level error. 
	private String field;
	
	// Error code. Typically the I18n message-code.
	private String code;
	
	// Error message
	private String message;
	
	
	public FieldError(String field, String code, String message) {
		this.field = field;
		this.code = code;
		this.message = message;
	}

	public String getField() {
		return field;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "FieldError {field=" + field + ", code=" + code + ", message=" + message + "}";
	}


	/**
	 * Converts a set of ConstraintViolations
	 * to a list of FieldErrors
	 * 
	 * @param constraintViolations
	 */
	public static List<FieldError> getErrors(
			Set<ConstraintViolation<?>> constraintViolations) {
		
		return constraintViolations.stream()
				.map(FieldError::of).collect(Collectors.toList());	
	}
	

	/**
	 * Converts a ConstraintViolation
	 * to a FieldError
	 */
	private static FieldError of(ConstraintViolation<?> constraintViolation) {
		
		// Get the field name by removing the first part of the propertyPath.
		// (The first part would be the service method name)
		String field = StringUtils.substringAfter(
				constraintViolation.getPropertyPath().toString(), ".");
		
		return new FieldError(field,
				constraintViolation.getMessageTemplate(),
				constraintViolation.getMessage());		
	}
}
