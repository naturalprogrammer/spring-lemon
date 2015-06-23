package com.naturalprogrammer.spring.boot.exceptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.naturalprogrammer.spring.boot.util.SaUtil;
import com.naturalprogrammer.spring.boot.validation.FieldError;

@ControllerAdvice
public class DefaultExceptionHandler {
	
    private Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(produces = "application/json")
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody Map<String, Object> handleConstraintViolationException(ConstraintViolationException ex) {
    	
		Collection<FieldError> errors = FieldError.getErrors(ex.getConstraintViolations());
		
    	log.error("ConstraintViolationException: " + errors.toString());
		return SaUtil.mapOf("exception", "ConstraintViolationException", "errors", errors);

    }

	@RequestMapping(produces = "application/json")
    @ExceptionHandler(MultiErrorException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody Map<String, Object> handleMultiErrorException(MultiErrorException ex) {
    	
		Collection<FieldError> errors = ex.getErrors();
		
    	log.error("MultiErrorException: " + errors.toString());
		return SaUtil.mapOf("exception", "MultiErrorException", "message", ex.getMessage(), "errors", errors);

    }

	
	@RequestMapping(produces = "application/json")
    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public @ResponseBody Map<String, Object> handleAuthorizationException(AccessDeniedException ex) {
    	
        log.error("User does not have proper rights:", ex);
		return SaUtil.mapOf("exception", "AccessDeniedException", "message", ex.getMessage());

    }

//	@RequestMapping(produces = "application/json")
//    @ExceptionHandler({BadRequestException.class})
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    public @ResponseBody Map<String, Object> handleRequestException(BadRequestException ex) {
//    	
//        log.error("BadRequestException:", ex);        
//		return SaUtil.mapOf("exception", "BadRequestException", "message", ex.getMessage());
//
//    }

	@RequestMapping(produces = "application/json")
    @ExceptionHandler({VersionException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public @ResponseBody Map<String, Object> handleRequestException(VersionException ex) {
    	
        log.error("VersionException:", ex);        
		return SaUtil.mapOf("exception", "VersionException", "message", ex.getMessage());

    }

	@RequestMapping(produces = "application/json")
    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody Map<String, Object> handleRequestException(Exception ex) {
    	
        log.error("Internal server error:", ex);        
		return SaUtil.mapOf("exception", ex.getClass().getSimpleName(), "message", ex.getMessage());

    }
	


}
