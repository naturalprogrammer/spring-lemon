package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;
import com.naturalprogrammer.spring.lemon.validation.FieldError;

/**
 * Exception handlers
 * 
 * @author Sanjay Patel
 */
//@RestControllerAdvice
//@RequestMapping(produces = "application/json")
public class DefaultExceptionHandler {
	
    private final Log log = LogFactory.getLog(getClass());

    
	/**
	 * Handles constraint violation exceptions
	 * 
	 * @param ex the exception
	 * @return the error response
	 */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolationException(ConstraintViolationException ex) {
    	
		Collection<FieldError> errors = FieldError.getErrors(ex.getConstraintViolations());
		
    	log.warn("ConstraintViolationException: " + errors.toString());
		return LemonUtil.mapOf("exception", "ConstraintViolationException", "errors", errors);
    }


	/**
	 * Handles multi-error exceptions
	 * 
	 * @param ex the exception
	 * @return the error response
	 */
    @ExceptionHandler(MultiErrorException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, Object>
		handleMultiErrorException(MultiErrorException ex) {
    	
		List<FieldError> errors = ex.getErrors();
		
    	log.warn("MultiErrorException: " + errors.toString());
		return LemonUtil.mapOf("exception", "MultiErrorException",
				"message", ex.getMessage(), "errors", errors);
    }
	
	
	/**
	 * Handles access-denied exceptions,
	 * typically from spring-security when a user
	 * tries to access a protected resource or method
	 * 
	 * @param ex the exception
	 * @return the error response
	 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public Map<String, Object>
	handleAuthorizationException(AccessDeniedException ex) {
    	
        log.warn("User does not have proper rights:", ex);
		return LemonUtil.mapOf("exception", "AccessDeniedException",
							   "message", ex.getMessage());
    }


	/**
	 * Handles version exceptions
	 * 
	 * @param ex the exception
	 * @return the error response
	 */
    @ExceptionHandler(VersionException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public Map<String, Object> handleVersionException(VersionException ex) {
    	
        log.warn("VersionException:", ex);        
		return LemonUtil.mapOf("exception", "VersionException", "message", ex.getMessage());
    }


	/**
	 * Handles any other exceptions
	 * 
	 * @param ex the exception
	 * @return the error response
	 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleOtherException(Exception ex) {
    	
        log.error("Internal server error:", ex);        
		return LemonUtil.mapOf("exception", ex.getClass().getSimpleName(), "message", ex.getMessage());
    }
}
