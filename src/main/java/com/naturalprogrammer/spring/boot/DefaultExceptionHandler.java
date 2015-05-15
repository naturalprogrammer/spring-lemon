package com.naturalprogrammer.spring.boot;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
		return Sa.mapOf("exception", "ConstraintViolationException", "errors", errors);

    }

	@RequestMapping(produces = "application/json")
    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody Map<String, Object> handleRequestException(Exception ex) {
    	
        log.error("Request error:", ex);
		return Sa.mapOf("error", "Request Error", "cause", ex.getMessage());

    }


}
