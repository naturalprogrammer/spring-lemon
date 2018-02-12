package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.LemonExceptionHandler;

public class ExceptionResponseComposer<T extends Throwable> {
	
    private static final Log log = LogFactory.getLog(LemonErrorAttributes.class);
	
	private final Map<String, LemonExceptionHandler<T>> handlers;
	
	public ExceptionResponseComposer(List<LemonExceptionHandler<T>> handlers) {
		
		this.handlers = handlers.stream().collect(
	            Collectors.toMap(LemonExceptionHandler::getExceptionName,
	            		Function.identity(), (handler1, handler2) -> {
	            			
	            			return AnnotationAwareOrderComparator
	            					.INSTANCE.compare(handler1, handler2) < 0 ?
	            					handler1 : handler2;
	            		}));
		
		log.info("Created");
	}

	
	public ExceptionResponseData compose(T ex) {

		LemonExceptionHandler<T> handler = null;
		
        // find a handler for the exception
        // if no handler is found,
        // loop into for its cause (ex.getCause())

		while (ex != null) {
			
			handler = handlers.get(ex.getClass().getSimpleName());
			
			if (handler != null) // found a handler
				break;
			
			ex = (T) ex.getCause();			
		}
        
        if (handler != null) { // a handler is found
        	
        	log.warn("Handling exception ", ex);       	
        	return new ExceptionResponseData(handler.getMessage(ex), handler.getStatus(ex), handler.getErrors(ex));
        }
        
        return new ExceptionResponseData(null, null, null);
	}
}
