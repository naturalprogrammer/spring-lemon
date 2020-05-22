package com.naturalprogrammer.spring.lemon.commonsweb.exceptions;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponse;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions thrown from in controllers or inner routines
 */
@RestControllerAdvice
public class DefaultExceptionHandlerControllerAdvice<T extends Throwable> {
	
	private static final Log log = LogFactory.getLog(DefaultExceptionHandlerControllerAdvice.class);

	/**
	 * Component that actually builds the error response
	 */
	private ErrorResponseComposer<T> errorResponseComposer;
	
    public DefaultExceptionHandlerControllerAdvice(ErrorResponseComposer<T> errorResponseComposer) {

		this.errorResponseComposer = errorResponseComposer;
		log.info("Created");
	}


	/**
     * Handles exceptions
     */
    @RequestMapping(produces = "application/json")
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleException(T ex) throws T {

    	ErrorResponse errorResponse = errorResponseComposer.compose(ex).orElseThrow(() -> ex);
    	
    	// Propogate up if message or status is null 
    	if (errorResponse.incomplete())
    		throw ex;
    	
    	log.warn("Handling exception", ex);
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(errorResponse.getStatus()));
    }
}
