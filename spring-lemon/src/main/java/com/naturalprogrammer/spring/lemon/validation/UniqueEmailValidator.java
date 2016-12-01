package com.naturalprogrammer.spring.lemon.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;

/**
 * Validator for unique-email
 * 
 * @author Sanjay Patel
 */
@Component
public class UniqueEmailValidator
implements ConstraintValidator<UniqueEmail, String> {

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private AbstractUserRepository<?,?> userRepository;
	
	@Override
	public void initialize(UniqueEmail constraintAnnotation) {
		log.debug("UniqueEmailValidator initialized");
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		
		log.debug("Validating whether email is unique: " + email);
		return !userRepository.findByEmail(email).isPresent();
	}

}
