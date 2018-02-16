package com.naturalprogrammer.spring.lemon.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class LemonValidatorFactoryBean<F extends LemonForm> extends LocalValidatorFactoryBean {
	
	@Override
	public void validate(Object target, Errors errors, final Object... validationHints) {
		
		super.validate(target, errors, validationHints);
		
		if (target instanceof LemonForm) {
			
			F form = (F) target;
			if (form.validator() != null) {
				
				form.validator().validate(form, errors, validationHints);
			}
		}
	}
}
