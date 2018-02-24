package com.naturalprogrammer.spring.lemon.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequestMapping(produces = "application/json")
public class DefaultExceptionHandlerControllerAdvice<T extends Throwable> {
	
	private static final Log log = LogFactory.getLog(DefaultExceptionHandlerControllerAdvice.class);

	private ErrorResponseComposer<T> errorResponseComposer;
	
    public DefaultExceptionHandlerControllerAdvice(ErrorResponseComposer<T> errorResponseComposer) {

		this.errorResponseComposer = errorResponseComposer;
		log.info("Created");
	}


	/**
     * Handles exceptions
     *
     * @param ex the exception
     * @return the error response
	 * @throws T 
     */
    @RequestMapping(produces = "application/json")
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleException(T ex) throws T {

    	ErrorResponse errorResponse = errorResponseComposer.compose(ex).orElseThrow(() -> ex);
    	
    	// Propogate up if message or status is null 
    	if (errorResponse.incomplete())
    		throw ex;
    	
    	log.warn("Handling exception", ex);
    	
    	// We didn't do this inside compose because LemonErrorAttributes would do it differently
    	errorResponse.setException(ex.getClass().getSimpleName());

        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(errorResponse.getStatus()));
    }
}
