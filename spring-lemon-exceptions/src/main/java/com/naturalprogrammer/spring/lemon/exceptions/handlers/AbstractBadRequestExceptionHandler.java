package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

/**
 * Extend this for any exception handler that should return a 400 response
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public abstract class AbstractBadRequestExceptionHandler<T extends Throwable> extends AbstractExceptionHandler<T> {

	public AbstractBadRequestExceptionHandler(Class<?> exceptionClass) {
		super(exceptionClass);
	}

	@Override
	public HttpStatus getStatus(T ex) {
		return HttpStatus.BAD_REQUEST;
	}
}
