package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebExchangeBindExceptionHandler extends AbstractExceptionHandler<WebExchangeBindException> {

	public WebExchangeBindExceptionHandler() {
		
		super(WebExchangeBindException.class.getSimpleName());
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(WebExchangeBindException ex) {
		return HttpStatus.UNPROCESSABLE_ENTITY;
	}
	
	@Override
	public Collection<LemonFieldError> getErrors(WebExchangeBindException ex) {
		return LemonFieldError.getErrors(ex);
	}
	
	@Override
	public String getMessage(WebExchangeBindException ex) {
		return LexUtils.getMessage("com.naturalprogrammer.spring.validationError");
	}
}
