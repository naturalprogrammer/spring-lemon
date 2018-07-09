package com.naturalprogrammer.spring.lemonreactive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;

@Configuration
@ComponentScan(basePackageClasses=AbstractExceptionHandler.class)
@EnableTransactionManagement
@EnableMongoAuditing
@EnableAsync
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AutoConfigureBefore({
	WebFluxAutoConfiguration.class,
	ErrorWebFluxAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	ReactiveSecurityAutoConfiguration.class,
	ReactiveUserDetailsServiceAutoConfiguration.class})
public class LemonReactiveAutoConfiguration {
	
	private static final Log log = LogFactory.getLog(LemonReactiveAutoConfiguration.class);
	
	public LemonReactiveAutoConfiguration() {
		log.info("Created");
	}
	
	/**
	 * Configures SecurityWebFilterChain if missing
	 */
	@Bean
	@ConditionalOnMissingBean(SecurityWebFilterChain.class)	
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		
		log.info("Configuring SecurityWebFilterChain ...");
		
		return http
			.authorizeExchange()
				.anyExchange().permitAll()
			.and()
				.formLogin()
			.and()
				.csrf().disable()
			.build();
	}
}
