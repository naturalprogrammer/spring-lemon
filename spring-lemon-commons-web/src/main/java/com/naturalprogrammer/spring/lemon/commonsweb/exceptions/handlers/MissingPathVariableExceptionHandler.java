package com.naturalprogrammer.spring.lemon.commonsweb.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingPathVariableException;

import java.util.Collection;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MissingPathVariableExceptionHandler extends AbstractExceptionHandler<MissingPathVariableException> {

	public MissingPathVariableExceptionHandler() {
		
		super(MissingPathVariableException.class);
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(MissingPathVariableException ex) {
		return HttpStatus.NOT_FOUND;
	}
}
