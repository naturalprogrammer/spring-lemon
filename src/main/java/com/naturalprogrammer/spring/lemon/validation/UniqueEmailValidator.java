package com.naturalprogrammer.spring.lemon.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;

/**
 * Reference
 *   http://www.captaindebug.com/2011/07/writng-jsr-303-custom-constraint_26.html#.VIVhqjGUd8E
 *   http://www.captechconsulting.com/blog/jens-alm/versioned-validated-and-secured-rest-services-spring-40-2?_ga=1.71504976.2113127005.1416833905
 * 
 * @author skpat_000
 *
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
		return userRepository.findByEmail(email) == null;

	}

}
