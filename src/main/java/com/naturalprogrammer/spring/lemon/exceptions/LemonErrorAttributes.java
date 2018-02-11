package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.LemonExceptionHandler;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

public class LemonErrorAttributes<T extends Throwable> extends DefaultErrorAttributes {
	
    private static final Log log = LogFactory.getLog(LemonErrorAttributes.class);

	private static final String ERRORS_KEY = "errors";
	static final String HTTP_STATUS_KEY = "httpStatus";
	
	private ExceptionResponseComposer<T> exceptionResponseComposer;
	
    public LemonErrorAttributes(ExceptionResponseComposer<T> exceptionResponseComposer) {

		this.exceptionResponseComposer = exceptionResponseComposer;
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
	protected void addLemonErrorDetails(
			Map<String, Object> errorAttributes, WebRequest request) {
		
		Throwable ex = getError(request);
		ExceptionResponseData exceptionResponseData = exceptionResponseComposer.compose((T)ex);
		
		String message = exceptionResponseData.getMessage();
		if (message != null)
			errorAttributes.put("message", message);
		
		Collection<FieldError> errors = exceptionResponseData.getErrors();
		if (errors != null)
			errorAttributes.put(ERRORS_KEY, errors);
		
		HttpStatus status = exceptionResponseData.getStatus();
		if (status != null) {
			errorAttributes.put(HTTP_STATUS_KEY, status);
			errorAttributes.put("status", status.value());
			errorAttributes.put("error", status.getReasonPhrase());
		}
	}
}
