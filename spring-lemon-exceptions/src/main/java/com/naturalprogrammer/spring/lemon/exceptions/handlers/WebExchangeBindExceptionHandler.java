package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Collection;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebExchangeBindExceptionHandler extends AbstractValidationExceptionHandler<WebExchangeBindException> {

	public WebExchangeBindExceptionHandler() {
		
		super(WebExchangeBindException.class);
		log.info("Created");
	}
	
	@Override
	public Collection<LemonFieldError> getErrors(WebExchangeBindException ex) {
		return LemonFieldError.getErrors(ex);
	}
}
