package com.naturalprogrammer.spring.lemon.exceptions.handlers;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MultiErrorExceptionHandler extends AbstractExceptionHandler<MultiErrorException> {

	private static final Log log = LogFactory.getLog(MultiErrorExceptionHandler.class);

	public MultiErrorExceptionHandler() {
		
		super(MultiErrorException.class.getSimpleName());
		log.info("Created");
	}
	
	@Override
	protected HttpStatus getStatus(MultiErrorException ex) {
		return ex.getStatus();
	}
	
	@Override
	protected Collection<FieldError> getErrors(MultiErrorException ex) {
		return ex.getErrors();
	}
}
