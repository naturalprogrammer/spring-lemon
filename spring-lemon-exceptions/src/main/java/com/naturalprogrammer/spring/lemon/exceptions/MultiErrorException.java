package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * An exception class which can contain multiple errors.
 * Used for validation, in service classes.
 * 
 * @author Sanjay Patel
 */
@Getter
public class MultiErrorException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	// list of errors
	private List<LemonFieldError> errors = new ArrayList<>(10);
	
	// HTTP Status code to be returned
	private HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
	
	// Set this if you need to customize exceptionId
	private String exceptionId = null;
	
	// Set this if you're doing bean validation and using validation groups
	@Getter(AccessLevel.NONE)
	private Class<?>[] validationGroups = {};
	
	/**
	 * Overrides the standard getMessage
	 */
	@Override
	public String getMessage() {

		if (errors.isEmpty())
			return null;
		
		// return the first message
		return errors.get(0).getMessage();
	}

	public MultiErrorException httpStatus(HttpStatus status) {
		this.status = status;
		return this;
	}

	public MultiErrorException exceptionId(String exceptionId) {
		this.exceptionId = exceptionId;
		return this;
	}

	public MultiErrorException validationGroups(Class<?>... groups) {
		validationGroups = groups;
		return this;
	}

	/**
	 * Adds a field-error if the given condition isn't true
	 */
	public MultiErrorException validateField(String fieldName, boolean valid,
			String messageKey, Object... args) {
		
		if (!valid)
			errors.add(new LemonFieldError(fieldName, messageKey,
				LexUtils.getMessage(messageKey, args)));
			
		return this;
	}

	/**
	 * Adds a global-error if the given condition isn't true
	 */
	public MultiErrorException validate(boolean valid,
			String messageKey, Object... args) {
		
		// delegate
		return validateField(null, valid, messageKey, args);
	}

	public <T> MultiErrorException validateBean(String beanName, T bean) {
		
		Set<? extends ConstraintViolation<T>> constraintViolations = 
				LexUtils.validator().validate(bean, validationGroups);
		
		addErrors(constraintViolations, beanName);
		return this;
	}

	/**
	 * Throws the exception, if there are accumulated errors
	 */
	public void go() {
		if (!errors.isEmpty())
			throw this;
	}	

	/**
	 * Adds constraint violations
	 * 
	 * @param constraintViolations
	 * @param objectName
	 * @return
	 */
	private void addErrors(Set<? extends ConstraintViolation<?>> constraintViolations, String objectName) {
		
		errors.addAll(constraintViolations.stream()
				.map(constraintViolation ->
					new LemonFieldError(
							objectName + "." + constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessageTemplate(),
							constraintViolation.getMessage()))
			    .collect(Collectors.toList()));
	}
}
