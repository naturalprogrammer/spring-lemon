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

@ControllerAdvice
public class DefaultExceptionHandler {
	
    private Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(produces = "application/json")
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody Map<String, Object> handleConstraintViolationException(ConstraintViolationException ex) {
    	
    	//TODO: Resume from here
    	// change the path from signup.signupForm.email to just email
    	// change the map processing to Java 8 style
    	// test multiple failures
    	// see if the response code is going properly
    	Map<String, Collection<String>> errors = new HashMap<String, Collection<String>>();
    	
    	//String[] emailErrors = {"Not valid email", "Error 2 on Email"};
    	//String[] formErrors = {"A form level error", "Error 2 on Form"};
    	
    	//errors.put("email", emailErrors);
    	//errors.put("", formErrors);
    	
    	for (ConstraintViolation<?> cv: ex.getConstraintViolations()) {
    		
    		
    		final StringBuilder fieldNameBuilder = new StringBuilder();
    		cv.getPropertyPath().forEach((item) -> {
    			fieldNameBuilder.setLength(0);
    			fieldNameBuilder.append(item.getName());
    		});
    		final String fieldName = fieldNameBuilder.toString();
    		
    		Collection<String> errorList = errors.get(fieldName);
    		if (errorList == null) {
    			errorList = new HashSet<String>();
        		errors.put(fieldName, errorList);    			
    		}
    		errorList.add(cv.getMessage());
    	}
    	
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
