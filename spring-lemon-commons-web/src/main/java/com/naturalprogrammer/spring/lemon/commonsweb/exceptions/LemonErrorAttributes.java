package com.naturalprogrammer.spring.lemon.commonsweb.exceptions;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

/**
 * Used for handling exceptions that can't be handled by
 * <code>DefaultExceptionHandlerControllerAdvice</code>,
 * e.g. exceptions thrown in filters.
 */
public class LemonErrorAttributes<T extends Throwable> extends DefaultErrorAttributes {
	
    private static final Log log = LogFactory.getLog(LemonErrorAttributes.class);

	static final String HTTP_STATUS_KEY = "httpStatus";
	
	private ErrorResponseComposer<T> errorResponseComposer;
	
    public LemonErrorAttributes(ErrorResponseComposer<T> errorResponseComposer) {

		this.errorResponseComposer = errorResponseComposer;
		log.info("Created");
	}
	
    /**
     * Calls the base class and then adds our details
     */
	@Override
	public Map<String, Object> getErrorAttributes(WebRequest request,
			boolean includeStackTrace) {
			
		Map<String, Object> errorAttributes =
				super.getErrorAttributes(request, includeStackTrace);
		
		addLemonErrorDetails(errorAttributes, request);
		
		return errorAttributes;
	}

	/**
     * Handles exceptions
     */
	@SuppressWarnings("unchecked")
	protected void addLemonErrorDetails(
			Map<String, Object> errorAttributes, WebRequest request) {
		
		Throwable ex = getError(request);
		
		errorResponseComposer.compose((T)ex).ifPresent(errorResponse -> {
			
			// check for null - errorResponse may have left something for the DefaultErrorAttributes
			
			if (errorResponse.getExceptionId() != null)
				errorAttributes.put("exceptionId", errorResponse.getExceptionId());

			if (errorResponse.getMessage() != null)
				errorAttributes.put("message", errorResponse.getMessage());
			
			Integer status = errorResponse.getStatus();
			
			if (status != null) {
				errorAttributes.put(HTTP_STATUS_KEY, status); // a way to pass response status to LemonErrorController
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
