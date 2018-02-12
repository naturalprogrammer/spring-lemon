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
public class DefaultExceptionHandler<T extends Throwable> {
	
	private static final Log log = LogFactory.getLog(DefaultExceptionHandler.class);

	private ErrorResponseComposer<T> errorResponseComposer;
	
    public DefaultExceptionHandler(ErrorResponseComposer<T> errorResponseComposer) {

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
    	if (errorResponse.getMessage() == null || errorResponse.getStatus() == null)
    		throw ex;
    	
    	log.warn("Handling exception", ex);
    	
    	errorResponse.setException(ex.getClass().getSimpleName());
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(errorResponse.getStatus()));
    }
}
