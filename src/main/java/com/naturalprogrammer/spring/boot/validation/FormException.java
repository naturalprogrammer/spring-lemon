package com.naturalprogrammer.spring.boot.validation;

import java.util.ArrayList;
import java.util.Collection;

import com.naturalprogrammer.spring.boot.util.SaUtil;

public class FormException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6020532846519363456L;
	
	private Collection<FieldError> fieldErrors = new ArrayList<FieldError>(10);
	
	public FormException(String field, String error) {
		addFieldError(field, error);		
	}

	public void addFieldError(String field, String error) {
		fieldErrors.add(FieldError.of(field, SaUtil.getMessage(error)));		
	}

	public Collection<FieldError> getFieldErrors() {
		return fieldErrors;
	}

}
