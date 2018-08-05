package com.naturalprogrammer.spring.lemon.exceptions.util;

import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.naturalprogrammer.spring.lemon.exceptions.ExplicitConstraintViolationException;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LexUtils {
	
	private static final Log log = LogFactory.getLog(LexUtils.class);

	private static MessageSource messageSource;
	private static LocalValidatorFactoryBean validator;
	public static final MultiErrorException NOT_FOUND_EXCEPTION = new MultiErrorException();

	/**
	 * Constructor
	 * 
	 * @param messageSource
	 */
	public LexUtils(MessageSource messageSource, LocalValidatorFactoryBean validator) {
		
		LexUtils.messageSource = messageSource;
		LexUtils.validator = validator;
		
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
	 * 
	 * @param messageKey	the key of the message
	 * @param args			any arguments
	 */
	public static String getMessage(String messageKey, Object... args) {
		
		if (messageSource == null)
			return "ApplicationContext unavailable, probably unit test going on";
		
		// http://stackoverflow.com/questions/10792551/how-to-obtain-a-current-user-locale-from-spring-without-passing-it-as-a-paramete
		return messageSource.getMessage(messageKey, args,
				LocaleContextHolder.getLocale());
	}	

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static <T> void validate(String name, T object, Class<?>... groups) {
		
		Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
		
		if (!violations.isEmpty())			
			throw new ExplicitConstraintViolationException(violations, name);
	}

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static MultiErrorException validate(
			boolean valid, String messageKey, Object... args) {
		
		return LexUtils.validate(null, valid, messageKey, args);
	}

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param fieldName		the name of the field related to the error
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static MultiErrorException validate(
			String fieldName, boolean valid, String messageKey, Object... args) {
		
		return new MultiErrorException().validate(fieldName, valid, messageKey, args);
	}

	
	/**
	 * Throws 404 Error is the entity isn't found
	 * 
	 * @param entity
	 */
	public static <T> void ensureFound(T entity) {
		
		LexUtils.validate(entity != null,
			"com.naturalprogrammer.spring.notFound")
			.httpStatus(HttpStatus.NOT_FOUND).go();
	}

	
	/**
	 * Supplys a 404 exception
	 */
	public static Supplier<MultiErrorException> notFoundSupplier() {
		
		return () -> NOT_FOUND_EXCEPTION;
	}
	

	public static String getRootExceptionName(Throwable ex) {
		
		if (ex == null)
			return "UnknownException";
			
		while(ex.getCause() != null)
			ex = ex.getCause();
		
		return ex.getClass().getSimpleName();
	}

}
