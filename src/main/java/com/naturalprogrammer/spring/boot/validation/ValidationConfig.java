package com.naturalprogrammer.spring.boot.validation;

import javax.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class ValidationConfig {

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
    	
        MethodValidationPostProcessor processor =
        		new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
    
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
	
}
