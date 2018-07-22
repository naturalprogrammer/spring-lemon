package com.naturalprogrammer.spring.lemondemo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class TestLemonFieldError {
	
	private String field;
	private String code;
	private String message;
}
