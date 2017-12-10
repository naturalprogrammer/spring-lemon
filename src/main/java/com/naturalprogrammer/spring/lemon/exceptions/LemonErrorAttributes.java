package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.LemonExceptionHandler;

public class LemonErrorAttributes extends DefaultErrorAttributes {
	
    private static final Log log = LogFactory.getLog(LemonErrorAttributes.class);
	
	private final Map<String, LemonExceptionHandler<?>> handlers;
	
	public LemonErrorAttributes(List<LemonExceptionHandler<?>> handlers) {
		
		this.handlers = handlers.stream().collect(
	            Collectors.toMap(LemonExceptionHandler::getExceptionName,
	            		Function.identity(), (handler1, handler2) -> {
	            			
	            			return AnnotationAwareOrderComparator
	            					.INSTANCE.compare(handler1, handler2) < 0 ?
	            					handler1 : handler2;
	            		}));
		
		log.info("Created");
	}

	
	@Override
	public Map<String, Object> getErrorAttributes(WebRequest request,
			boolean includeStackTrace) {
			
		Map<String, Object> errorAttributes =
				super.getErrorAttributes(request, includeStackTrace);
		
		addLemonErrorDetails(errorAttributes, request);
		return errorAttributes;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Throwable> void addLemonErrorDetails(
			Map<String, Object> errorAttributes, WebRequest request) {
		
		Throwable ex = getError(request);
		
		LemonExceptionHandler<T> handler = null;
		
        // find a handler for the exception
        // if no handler is found,
        // loop into for its cause (ex.getCause())

		while (ex != null) {
			
			handler = (LemonExceptionHandler<T>) handlers.get(ex.getClass().getSimpleName());
			
			if (handler != null) // found a handler
				break;
			
			ex = ex.getCause();			
		}
        
        if (handler != null) { // a handler is found
        	
        	log.warn("Handling exception ", ex);
        	
	        // Use the handler to add errors and update status
	        handler.putErrorDetails(errorAttributes, (T) ex);
        }
	}
}
