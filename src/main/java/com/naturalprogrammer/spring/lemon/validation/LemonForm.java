package com.naturalprogrammer.spring.lemon.validation;

public interface LemonForm {
	
	default <F extends LemonForm> LemonValidator<F> validator() {
		
		return null;
	}
}
