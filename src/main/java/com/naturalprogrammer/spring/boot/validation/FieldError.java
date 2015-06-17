package com.naturalprogrammer.spring.boot.validation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;

import com.naturalprogrammer.spring.boot.util.Ref;

public class FieldError {
	
	private String field;
	private String error;

	public String getField() {
		return field;
	}

	public String getError() {
		return error;
	}

	@Override
	public String toString() {
		return "FieldError [field=" + field + ", error=" + error + "]";
	}

	public static Collection<FieldError> getErrors(Set<ConstraintViolation<?>> constraintViolations) {
		
		return constraintViolations.stream().map(FieldError::of).collect(Collectors.toList());
		
	}
	
	private static FieldError of(ConstraintViolation<?> constraintViolation) {
		
		return FieldError.of(
			computeFieldName(constraintViolation),
			constraintViolation.getMessage());
		
	}
	
	

	private static String computeFieldName(ConstraintViolation<?> constraintViolation) {
		
		final Ref<Node> lastNode = new Ref<Node>();
		
		constraintViolation.getPropertyPath().forEach((node) -> {
			lastNode.value = node;
		});
		
		return lastNode.value.getName();
		
	}

	public static FieldError of(String field, String error) {
		
		FieldError fieldError = new FieldError();
		fieldError.field = field;
		fieldError.error = error;
		
		return fieldError;
	}

}
