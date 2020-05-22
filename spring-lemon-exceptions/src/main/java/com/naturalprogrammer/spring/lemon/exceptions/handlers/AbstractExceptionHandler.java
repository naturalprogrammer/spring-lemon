package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponse;
import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;

import java.util.Collection;

/**
 * Extend this to code an exception handler
 */
public abstract class AbstractExceptionHandler<T extends Throwable> {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private Class<?> exceptionClass;
	
	public AbstractExceptionHandler(Class<?> exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	public Class<?> getExceptionClass() {
		return exceptionClass;
	}
	
	protected String getExceptionId(T ex) {
		return LexUtils.getExceptionId(ex);
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
		
		errorResponse.setExceptionId(getExceptionId(ex));
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
