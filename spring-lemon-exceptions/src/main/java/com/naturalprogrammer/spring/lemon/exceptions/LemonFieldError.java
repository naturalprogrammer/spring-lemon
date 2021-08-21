/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.spring.lemon.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;

import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds a field or form error
 */
@Getter @AllArgsConstructor @ToString
public class LemonFieldError implements Serializable {
	
	// Name of the field. Null in case of a form level error. 
	private final String field;
	
	// Error code. Typically the I18n message-code.
	private final String code;
	
	// Error message
	private final String message;

	/**
	 * Converts a set of ConstraintViolations
	 * to a list of FieldErrors
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
