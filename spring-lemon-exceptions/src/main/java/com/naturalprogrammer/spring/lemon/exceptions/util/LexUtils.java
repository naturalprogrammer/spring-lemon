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

package com.naturalprogrammer.spring.lemon.exceptions.util;

import com.naturalprogrammer.spring.lemon.exceptions.ExceptionIdMaker;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.function.Supplier;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
@Slf4j
public class LexUtils {

	private static MessageSource messageSource;
	private static LocalValidatorFactoryBean validator;
	private static ExceptionIdMaker exceptionIdMaker;

	private static final Validator DEFAULT_VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	public static final ExceptionIdMaker EXCEPTION_ID_MAKER = ex -> {

		if (ex == null)
			return null;

		return ex.getClass().getSimpleName();
	};


	public static final MultiErrorException NOT_FOUND_EXCEPTION = new MultiErrorException();

	/**
	 * Constructor
	 */
	public LexUtils(MessageSource messageSource,
			LocalValidatorFactoryBean validator,
			ExceptionIdMaker exceptionIdMaker) {
		
		LexUtils.messageSource = messageSource;
		LexUtils.validator = validator;
		LexUtils.exceptionIdMaker = exceptionIdMaker;
		
		log.info("Created");
	}

	
	@PostConstruct
	public void postConstruct() {
		
		NOT_FOUND_EXCEPTION
			.httpStatus(HttpStatus.NOT_FOUND)
			.validate(false, "com.naturalprogrammer.spring.notFound");
		
		log.info("NOT_FOUND_EXCEPTION built");		
	}

	
	/**
	 * Gets a message from messages.properties
	 */
	public static String getMessage(String messageKey, Object... args) {
		
		if (messageSource == null) // ApplicationContext unavailable, probably unit test going on
			return messageKey;
		
		// http://stackoverflow.com/questions/10792551/how-to-obtain-a-current-user-locale-from-spring-without-passing-it-as-a-paramete
		return messageSource.getMessage(messageKey, args,
				LocaleContextHolder.getLocale());
	}	

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 */
	public static MultiErrorException validate(
			boolean valid, String messageKey, Object... args) {
		
		return validateField(null, valid, messageKey, args);
	}

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 */
	public static MultiErrorException validateField(
			String fieldName, boolean valid, String messageKey, Object... args) {
		
		return new MultiErrorException().validateField(fieldName, valid, messageKey, args);
	}

	
	/**
	 * Creates a MultiErrorException out of the constraint violations in the given bean
	 */
	public static <T> MultiErrorException validateBean(String beanName, T bean, Class<?>... validationGroups) {
		
		return new MultiErrorException()
			.exceptionId(getExceptionId(new ConstraintViolationException(null)))
			.validationGroups(validationGroups)
			.validateBean(beanName, bean);
	}

	
	/**
	 * Throws 404 Error is the entity isn't found
	 */
	public static <T> void ensureFound(T entity) {
		
		validate(entity != null,
			"com.naturalprogrammer.spring.notFound")
			.httpStatus(HttpStatus.NOT_FOUND).go();
	}

	
	/**
	 * Supplys a 404 exception
	 */
	public static Supplier<MultiErrorException> notFoundSupplier() {
		
		return () -> NOT_FOUND_EXCEPTION;
	}
	

	public static String getExceptionId(Throwable ex) {
		
		Throwable root = getRootException(ex);

		if (exceptionIdMaker == null) // in unit tests
			return EXCEPTION_ID_MAKER.make(ex);

		return exceptionIdMaker.make(root);
	}
	
	
	private static Throwable getRootException(Throwable ex) {

		if (ex == null)
			return null;

		while(ex.getCause() != null)
			ex = ex.getCause();
		
		return ex;
	}

	
	public static Validator validator() {
		return validator == null ? // e.g. in unit tests
				DEFAULT_VALIDATOR : validator;
	}
}
