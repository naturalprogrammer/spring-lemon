package com.naturalprogrammer.spring.lemon.validation;

import org.springframework.validation.Errors;

public interface LemonValidator<F extends LemonForm> {

	public void validate(F form, Errors errors, final Object... validationHints);
}
