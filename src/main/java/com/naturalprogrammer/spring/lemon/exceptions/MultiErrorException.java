package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

/**
 * An exception class which can contain multiple errors.
 * Used for validation, in service classes.
 * 
 * @author Sanjay Patel
 */
public class MultiErrorException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	// HTTP Status code to be returned
	private HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
	
	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public MultiErrorException httpStatus(HttpStatus status) {
		this.status = status;
		return this;
	}

	// list of errors
	private List<FieldError> errors = new ArrayList<FieldError>(10);
	
	public List<FieldError> getErrors() {
		return errors;
	}	
	
	/**
	 * Overrides the standard getMessage
	 */
	@Override
	public String getMessage() {

		if (errors.size() == 0)
			return null;
		
		// return the first message
		return errors.get(0).getMessage();
	}
	
	/**
	 * Adds a global-error if the given condition isn't true
	 */
	public MultiErrorException validate(boolean valid,
			String messageKey, Object... args) {
		
		// delegate
		return validate(null, valid, messageKey, args);
	}

	/**
	 * Adds a field-error if the given condition isn't true
	 */
	public MultiErrorException validate(String fieldName, boolean valid,
			String messageKey, Object... args) {
		
		if (!valid)
			errors.add(new FieldError(fieldName, messageKey,
				LemonUtils.getMessage(messageKey, args)));
			
		return this;
	}

	/**
	 * Throws the exception, if there are accumulated errors
	 */
	public void go() {
		if (errors.size() > 0)
			throw this;
	}
}
