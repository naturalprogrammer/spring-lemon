package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

public abstract class AbstractExceptionHandler<T extends Throwable> implements LemonExceptionHandler<T> {
	
	private String exceptionName;
	
	public AbstractExceptionHandler(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	@Override
	public String getExceptionName() {
		return exceptionName;
	}
	
	@Override
	public void putErrorDetails(Map<String, Object> errorAttributes, T ex) {
		
		String messageKey = getMessageKey(ex);
		if (messageKey != null)
			errorAttributes.put("message", LemonUtil.getMessage(messageKey));
		
		Collection<FieldError> errors = getErrors(ex);
		if (errors != null)
			errorAttributes.put(ERRORS_KEY, errors);
		
		HttpStatus status = getStatus(ex);
		if (status != null) {
			errorAttributes.put(HTTP_STATUS_KEY, status);
			errorAttributes.put("status", status.value());
			errorAttributes.put("error", status.getReasonPhrase());
		}
	}

	protected String getMessageKey(T ex) {
		return null;
	}
	
	protected HttpStatus getStatus(T ex) {
		return null;
	}
	
	protected Collection<FieldError> getErrors(T ex) {
		return null;
	}
}
