package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.Collection;

/**
 * Error DTO, to be sent as response body
 * in case of errors
 */
public class ErrorResponse {
	
	private String exception;
	private String error;
	private String message;
	private Integer status; // We'd need it as integer in JSON serialization
	private Collection<FieldError> errors;
	
	public boolean incomplete() {
		
		return message == null || status == null;
	}
	
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Collection<FieldError> getErrors() {
		return errors;
	}
	public void setErrors(Collection<FieldError> errors) {
		this.errors = errors;
	}
}
