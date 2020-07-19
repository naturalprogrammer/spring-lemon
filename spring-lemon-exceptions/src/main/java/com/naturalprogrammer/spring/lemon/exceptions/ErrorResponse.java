package com.naturalprogrammer.spring.lemon.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * Error DTO, to be sent as response body
 * in case of errors
 */
@Getter @Setter @ToString
public class ErrorResponse {

	private String id;
	private String exceptionId;
	private String error;
	private String message;
	private Integer status; // We'd need it as integer in JSON serialization
	private Collection<LemonFieldError> errors;
	
	public boolean incomplete() {
		
		return message == null || status == null;
	}
}
