package com.naturalprogrammer.spring.lemon.commons.exceptions.handlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractBadRequestExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class JsonParseExceptionHandler extends AbstractBadRequestExceptionHandler<JsonParseException> {

	public JsonParseExceptionHandler() {
		
		super(JsonParseException.class);
		log.info("Created");
	}
}
