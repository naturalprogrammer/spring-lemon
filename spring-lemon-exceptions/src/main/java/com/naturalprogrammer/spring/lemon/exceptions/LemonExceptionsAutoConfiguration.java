package com.naturalprogrammer.spring.lemon.exceptions;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

@Configuration
@AutoConfigureBefore({ValidationAutoConfiguration.class})
@ComponentScan(basePackageClasses=AbstractExceptionHandler.class)
@Slf4j
public class LemonExceptionsAutoConfiguration {

	public LemonExceptionsAutoConfiguration() {
		log.info("Created");
	}
	
	
	/**
	 * Configures ErrorResponseComposer if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorResponseComposer.class)
	public <T extends Throwable>
	ErrorResponseComposer<T> errorResponseComposer(List<AbstractExceptionHandler<T>> handlers) {
		
        log.info("Configuring ErrorResponseComposer");       
		return new ErrorResponseComposer<T>(handlers);
	}

	
	/**
	 * Configures ExceptionCodeMaker if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ExceptionIdMaker.class)
	public ExceptionIdMaker exceptionIdMaker() {
		
        log.info("Configuring ExceptionIdMaker");
        return LexUtils.EXCEPTION_ID_MAKER;
	}

	
	/**
	 * Configures LexUtils
	 */
	@Bean
	public LexUtils lexUtils(MessageSource messageSource,
			LocalValidatorFactoryBean validator,
			ExceptionIdMaker exceptionIdMaker) {

        log.info("Configuring LexUtils");       		
		return new LexUtils(messageSource, validator, exceptionIdMaker);
	}
}
