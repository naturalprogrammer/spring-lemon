package com.naturalprogrammer.spring.lemon.validation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;

public class FieldError {
	
	private String field;
	private String code;
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

	public static List<FieldError> getErrors(
			Set<ConstraintViolation<?>> constraintViolations) {
		
		return constraintViolations.stream()
				.map(FieldError::of).collect(Collectors.toList());	
	}
	
	private static FieldError of(ConstraintViolation<?> constraintViolation) {
		
		String field = StringUtils.substringAfter(
				constraintViolation.getPropertyPath().toString(), ".");
		
		return new FieldError(field, constraintViolation.getMessageTemplate(), constraintViolation.getMessage());		
	}

//	private static String computeFieldName(ConstraintViolation<?> constraintViolation) {
//		
//		// The first component of the path is 
//		// the method name. Remove it. 
//		return StringUtils.substringAfter(
//			constraintViolation.getPropertyPath().toString(), ".");
//		
//	}
//
//	public static FieldError of(String field, String message) {
//		
//		FieldError fieldError = new FieldError();
//		fieldError.field = field;
//		fieldError.message = message;
//		
//		return fieldError;
//	}

}
