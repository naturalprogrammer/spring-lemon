package com.naturalprogrammer.spring.lemondemo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Error DTO, to be sent as response body
 * in case of errors
 */
@Getter @Setter
public class TestErrorResponse {
	
	private String exception;
	private String error;
	private String message;
	private Integer status; // We'd need it as integer in JSON serialization
	private Collection<TestLemonFieldError> errors;
}
