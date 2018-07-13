package com.naturalprogrammer.spring.lemonreactive.validation;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.naturalprogrammer.spring.lemonreactive.LemonReactiveService;

/**
 * Validator for unique-email
 * 
 * @author Sanjay Patel
 */
public class UniqueEmailValidator
implements ConstraintValidator<UniqueEmail, String> {

	private static final Log log = LogFactory.getLog(UniqueEmailValidator.class);

	private MongoTemplate mongoTemplate;
	private Class<?> userClass;

	public UniqueEmailValidator(MongoTemplate mongoTemplate,
			LemonReactiveService<?,?> lemonReactiveService) {
		
		this.mongoTemplate = mongoTemplate;
		this.userClass = lemonReactiveService.newUser().getClass();
		log.info("Created");
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		
		log.debug("Validating whether email is unique: " + email);
		return !mongoTemplate.exists(query(where("email").is(email)), userClass);
	}
}
