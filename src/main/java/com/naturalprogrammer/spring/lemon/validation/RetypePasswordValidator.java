package com.naturalprogrammer.spring.lemon.validation;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

/**
 * References
 * 
 *   http://docs.jboss.org/hibernate/validator/4.1/reference/en-US/html/validator-usingvalidator.html#d0e326
 *   http://docs.jboss.org/hibernate/validator/4.1/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 * 
 * @author skpat_000
 *
 */
@Component
public class RetypePasswordValidator implements ConstraintValidator<RetypePassword, RetypePasswordForm> {
	
	@Override
	public boolean isValid(RetypePasswordForm retypePasswordForm, ConstraintValidatorContext context) {
		
		if (!Objects.equals(retypePasswordForm.getPassword(), retypePasswordForm.getRetypePassword())) {
			
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("{com.naturalprogrammer.spring.same.passwords}")
				.addPropertyNode("retypePassword").addConstraintViolation();
			
			return false;
			
		}
		
		return true;
	}

	@Override
	public void initialize(RetypePassword constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

}
