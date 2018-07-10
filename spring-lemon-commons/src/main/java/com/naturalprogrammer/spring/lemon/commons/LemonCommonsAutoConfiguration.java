package com.naturalprogrammer.spring.lemon.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.naturalprogrammer.spring.lemon.commons.exceptions.handlers.BadCredentialsExceptionHandler;

@Configuration
@ComponentScan(basePackageClasses=BadCredentialsExceptionHandler.class)
@EnableAsync
public class LemonCommonsAutoConfiguration {

	private static final Log log = LogFactory.getLog(LemonCommonsAutoConfiguration.class);
	
	public LemonCommonsAutoConfiguration() {
		log.info("Created");
	}
	
	
	/**
	 * Configures Password encoder if missing
	 */
	@Bean
	@ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
	
		log.info("Configuring PasswordEncoder");		
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
