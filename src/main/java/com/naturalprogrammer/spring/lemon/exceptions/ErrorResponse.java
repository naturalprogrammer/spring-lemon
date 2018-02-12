package com.naturalprogrammer.spring.lemon.exceptions;

import java.util.Collection;

import org.springframework.http.HttpStatus;

import com.naturalprogrammer.spring.lemon.validation.FieldError;

public class ExceptionResponseData {
	
	private String error;
	private String message;
	private HttpStatus status;
	private Collection<FieldError> errors;
	
	public ExceptionResponseData(String message, HttpStatus status, Collection<FieldError> errors) {

		this.message = message;
		this.status = status;
		this.errors = errors;
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
	public HttpStatus getStatus() {
		return status;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	public Collection<FieldError> getErrors() {
		return errors;
	}
	public void setErrors(Collection<FieldError> errors) {
		this.errors = errors;
	}	
}
