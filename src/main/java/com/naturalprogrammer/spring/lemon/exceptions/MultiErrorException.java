package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

public class MultiErrorException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	private List<FieldError> errors = new ArrayList<FieldError>(10);
	
	/**
	 * This is public because more than one error can be added 
	 * 
	 * @param field
	 * @param error
	 * @param args
	 */
	private MultiErrorException addError(String field, String error, Object... args) {
		errors.add(FieldError.of(field, LemonUtil.getMessage(error, args)));
		return this;
	}

	public List<FieldError> getErrors() {
		return errors;
	}
	
	@Override
	public String getMessage() {

		if (errors.size() == 0)
			return null;
		
		return errors.get(0).getMessage();
		
	}
	
	public MultiErrorException check(boolean valid, String messageKey, Object... args) {
		return check(null, valid, messageKey, args);
	}

	public MultiErrorException check(String fieldName, boolean valid, String messageKey, Object... args) {
		return valid ? this : addError(fieldName, messageKey, args);
	}
	
	public void go() {
		if (errors.size() > 0)
			throw this;
	}

}
