package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

@Order(Ordered.LOWEST_PRECEDENCE)
public abstract class AbstractBadRequestExceptionHandler<T extends Throwable> extends AbstractExceptionHandler<T> {

	public AbstractBadRequestExceptionHandler(String exceptionName) {
		super(exceptionName);
	}

	@Override
	public HttpStatus getStatus(T ex) {
		return HttpStatus.BAD_REQUEST;
	}
}
