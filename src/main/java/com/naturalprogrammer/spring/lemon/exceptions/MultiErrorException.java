package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

public class MultiErrorException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	private List<FieldError> errors = new ArrayList<FieldError>(10);
	
	public List<FieldError> getErrors() {
		return errors;
	}
	
	@Override
	public String getMessage() {

		if (errors.size() == 0)
			return null;
		
		return errors.get(0).getMessage();
	}
	
	public MultiErrorException check(boolean valid,
			String messageKey, Object... args) {
		
		return check(null, valid, messageKey, args);
	}

	public MultiErrorException check(String fieldName, boolean valid,
			String messageKey, Object... args) {
		
		if (!valid)
			errors.add(new FieldError(fieldName, messageKey,
				LemonUtil.getMessage(messageKey, args)));
			
		return this;
	}
	
	public static MultiErrorException of(String fieldName, 
			String messageKey, Object... args) {
		
		MultiErrorException exception = new MultiErrorException();
		exception.errors.add(new FieldError(fieldName, messageKey,
				LemonUtil.getMessage(messageKey, args)));
		return exception;
	}
	
	public static MultiErrorException of(String messageKey, Object... args) {
		
		return MultiErrorException.of(null, messageKey, args);
	}
	
	public void go() {
		if (errors.size() > 0)
			throw this;
	}

}
