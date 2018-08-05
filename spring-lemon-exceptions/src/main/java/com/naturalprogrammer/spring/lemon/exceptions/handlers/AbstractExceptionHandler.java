package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponse;
import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;

/**
 * Extend this to code an exception handler
 */
public abstract class AbstractExceptionHandler<T extends Throwable> {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private String exceptionName;
	
	public AbstractExceptionHandler(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	public String getExceptionName() {
		return exceptionName;
	}
	
	protected String getMessage(T ex) {
		return ex.getMessage();
	}
	
	protected HttpStatus getStatus(T ex) {
		return null;
	}
	
	protected Collection<LemonFieldError> getErrors(T ex) {
		return null;
	}

	public ErrorResponse getErrorResponse(T ex) {
		
		ErrorResponse errorResponse = new ErrorResponse();
		
		errorResponse.setMessage(getMessage(ex));
		
		HttpStatus status = getStatus(ex);
		if (status != null) {
			errorResponse.setStatus(status.value());
			errorResponse.setError(status.getReasonPhrase());
		}
		
		errorResponse.setErrors(getErrors(ex));
		return errorResponse;
	}
}
