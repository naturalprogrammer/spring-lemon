/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
