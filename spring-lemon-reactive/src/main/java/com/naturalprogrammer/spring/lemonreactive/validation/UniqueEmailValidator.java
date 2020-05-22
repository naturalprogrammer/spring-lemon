package com.naturalprogrammer.spring.lemonreactive.validation;

import com.naturalprogrammer.spring.lemonreactive.LemonReactiveService;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Validator for unique-email
 * 
 * @author Sanjay Patel
 */
public class UniqueEmailValidator
implements ConstraintValidator<UniqueEmail, String> {

	private static final Log log = LogFactory.getLog(UniqueEmailValidator.class);

	private ReactiveMongoTemplate mongoTemplate;
	private Class<?> userClass;

	public UniqueEmailValidator(ReactiveMongoTemplate mongoTemplate,
			LemonReactiveService<?,?> lemonReactiveService) {
		
		this.mongoTemplate = mongoTemplate;
		this.userClass = lemonReactiveService.newUser().getClass();
		log.info("Created");
	}

	@SneakyThrows
	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {

		log.debug("Validating whether email is unique: " + email);

		final AtomicBoolean unique = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(1);

		mongoTemplate.exists(query(where("email").is(email)), userClass)
				.subscribe(exists -> {
					unique.set(!exists);
					latch.countDown();
				});

		latch.await();
		return unique.get();
	}
}
