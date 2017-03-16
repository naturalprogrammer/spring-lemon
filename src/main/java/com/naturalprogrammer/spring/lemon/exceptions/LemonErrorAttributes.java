package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.LemonExceptionHandler;

@Component
public class LemonErrorAttributes extends DefaultErrorAttributes {
	
    private static final Log log = LogFactory.getLog(LemonErrorAttributes.class);
	
	Map<String, LemonExceptionHandler<?>> handlers;
	
	public LemonErrorAttributes() {
		log.info("Created");
	}

	@Autowired
	public void setHandlers(List<LemonExceptionHandler<?>> handlers) {
		
		log.info("Setting handlers ...");

		this.handlers = handlers.stream().collect(
            Collectors.toMap(LemonExceptionHandler::getExceptionName,
            		Function.identity(), (handler1, handler2) -> {
            			
            			return AnnotationAwareOrderComparator
            					.INSTANCE.compare(handler1, handler2) < 0 ?
            					handler1 : handler2;
            		}));
	}
	
	@Override
	public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
			boolean includeStackTrace) {
			
		Map<String, Object> errorAttributes =
				super.getErrorAttributes(requestAttributes, includeStackTrace);
		
		addLemonErrorDetails(errorAttributes, requestAttributes);
		return errorAttributes;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Throwable> void addLemonErrorDetails(
			Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
		
		Throwable ex = getError(requestAttributes);
		
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
