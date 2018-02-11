package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.validation.FieldError;

public abstract class AbstractExceptionHandler<T extends Throwable> implements LemonExceptionHandler<T> {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	private String exceptionName;
	
	public AbstractExceptionHandler(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	@Override
	public String getExceptionName() {
		return exceptionName;
	}
	
	@Override
	public String getMessage(T ex) {
		return null;
	}
	
	@Override
	public HttpStatus getStatus(T ex) {
		return null;
	}
	
	@Override
	public Collection<FieldError> getErrors(T ex) {
		return null;
	}
}
