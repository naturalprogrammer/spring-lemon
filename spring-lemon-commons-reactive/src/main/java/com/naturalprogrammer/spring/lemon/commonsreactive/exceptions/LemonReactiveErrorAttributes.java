package com.naturalprogrammer.spring.lemon.commonsreactive.exceptions;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

public class LemonReactiveErrorAttributes<T extends Throwable> extends DefaultErrorAttributes {
	
	private static final Log log = LogFactory.getLog(LemonReactiveErrorAttributes.class);

	/**
	 * Component that actually builds the error response
	 */
	private ErrorResponseComposer<T> errorResponseComposer;
	
    public LemonReactiveErrorAttributes(ErrorResponseComposer<T> errorResponseComposer) {

		this.errorResponseComposer = errorResponseComposer;
		log.info("Created");
	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request,
			ErrorAttributeOptions options) {
		
		Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
		addLemonErrorDetails(errorAttributes, request);
		return errorAttributes;
	}
	
	/**
     * Handles exceptions
     */
	@SuppressWarnings("unchecked")
	protected void addLemonErrorDetails(
			Map<String, Object> errorAttributes, ServerRequest request) {
		
		Throwable ex = getError(request);
		
		errorResponseComposer.compose((T)ex).ifPresent(errorResponse -> {
			
			// check for nulls - errorResponse may have left something for the DefaultErrorAttributes
			
			if (errorResponse.getExceptionId() != null)
				errorAttributes.put("exceptionId", errorResponse.getExceptionId());

			if (errorResponse.getMessage() != null)
				errorAttributes.put("message", errorResponse.getMessage());
			
			Integer status = errorResponse.getStatus();
			
			if (status != null) {
				errorAttributes.put("status", status);
				errorAttributes.put("error", errorResponse.getError());
			}

			if (errorResponse.getErrors() != null)
				errorAttributes.put("errors", errorResponse.getErrors());			
		});
		
		if (errorAttributes.get("exceptionId") == null)
			errorAttributes.put("exceptionId", LexUtils.getExceptionId(ex));		
	}
}
