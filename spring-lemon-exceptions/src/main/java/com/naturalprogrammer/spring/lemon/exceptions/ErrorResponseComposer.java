package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

/**
 * Given an exception, builds a response.
 */
public class ErrorResponseComposer<T extends Throwable> {
	
    private static final Log log = LogFactory.getLog(ErrorResponseComposer.class);
	
	private final Map<Class<?>, AbstractExceptionHandler<T>> handlers;
	
	public ErrorResponseComposer(List<AbstractExceptionHandler<T>> handlers) {
		
		this.handlers = handlers.stream().collect(
	            Collectors.toMap(AbstractExceptionHandler::getExceptionClass,
	            		Function.identity(), (handler1, handler2) -> {
	            			
	            			return AnnotationAwareOrderComparator
	            					.INSTANCE.compare(handler1, handler2) < 0 ?
	            					handler1 : handler2;
	            		}));
		
		log.info("Created");
	}

	/**
	 * Given an exception, finds a handler for 
	 * building the response and uses that to build and return the response
	 */
	public Optional<ErrorResponse> compose(T ex) {

		AbstractExceptionHandler<T> handler = null;
		
        // find a handler for the exception
        // if no handler is found,
        // loop into for its cause (ex.getCause())

		while (ex != null) {
			
			handler = handlers.get(ex.getClass());
			
			if (handler != null) // found a handler
				break;
			
			ex = (T) ex.getCause();			
		}
        
        if (handler != null) // a handler is found    	
        	return Optional.of(handler.getErrorResponse(ex));
        
        return Optional.empty();
	}
}
