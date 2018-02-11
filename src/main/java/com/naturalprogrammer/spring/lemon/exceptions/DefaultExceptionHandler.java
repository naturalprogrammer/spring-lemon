package com.naturalprogrammer.spring.lemon.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequestMapping(produces = "application/json")
public class DefaultExceptionHandler {
	
	private static final Log log = LogFactory.getLog(DefaultExceptionHandler.class);

	private ExceptionResponseComposer<?> exceptionResponseComposer;
	
    public DefaultExceptionHandler(ExceptionResponseComposer<?> exceptionResponseComposer) {

		this.exceptionResponseComposer = exceptionResponseComposer;
		log.info("Created");
	}


	/**
     * Handles exceptions
     *
     * @param ex the exception
     * @return the error response
     */
    @RequestMapping(produces = "application/json")
    public ResponseEntity<?> handleException(Throwable ex) {

        AbstractExceptionHandler handler;

        // find a handler for the exception
        // if no handler is found,
        // loop into for its cause (ex.getCause())
        do {
            handler = handlers.get(ex.getClass().getSimpleName());

            if (handler != null) // a handler is found!
                break;

            if (ex.getCause() == null) { // no underlying cause to dig into
                handler = handlers.get(Exception.class.getSimpleName()); // default to Exception
                break;
            }

            ex = ex.getCause(); // let's dig into the underlying cause

        } while (true);

        log.warn("Exception: ", ex);

        // Use the handler to get an Error object to be sent to client
        Error error = handler.getError(ex);

        return new ResponseEntity<ExceptionResponseData>(error, error.getStatus(ex));
    }
}
