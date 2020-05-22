package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

/**
 * Extend this for any exception handler that should return a 400 response
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public abstract class AbstractValidationExceptionHandler<T extends Throwable> extends AbstractExceptionHandler<T> {

	public AbstractValidationExceptionHandler(Class<?> exceptionClass) {
		super(exceptionClass);
	}

	@Override
	public HttpStatus getStatus(T ex) {
		return HttpStatus.UNPROCESSABLE_ENTITY;
	}
	
	@Override
	public String getMessage(T ex) {
		return LexUtils.getMessage("com.naturalprogrammer.spring.validationError");
	}
}
