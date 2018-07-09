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
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.LemonExceptionsAutoConfiguration;
import com.naturalprogrammer.spring.lemonreactive.exceptions.LemonReactiveErrorAttributes;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveAuthenticationFailureHandler;

@Configuration
@EnableTransactionManagement
@EnableMongoAuditing
@AutoConfigureBefore({
	WebFluxAutoConfiguration.class,
	ErrorWebFluxAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	ReactiveSecurityAutoConfiguration.class,
	ReactiveUserDetailsServiceAutoConfiguration.class,
	LemonExceptionsAutoConfiguration.class})
public class LemonReactiveAutoConfiguration {
	
	private static final Log log = LogFactory.getLog(LemonReactiveAutoConfiguration.class);
	
	public LemonReactiveAutoConfiguration() {
		log.info("Created");
	}
	
	
	/**
	 * Configures an Error Attributes if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorAttributes.class)
	public <T extends Throwable>
	ErrorAttributes errorAttributes(ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring LemonErrorAttributes");       
		return new LemonReactiveErrorAttributes<T>(errorResponseComposer);
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
					.loginPage("/login") // Should be by default, but not providing this overwrites our AuthenticationFailureHandler, because this is called later 
					.authenticationFailureHandler(new LemonReactiveAuthenticationFailureHandler())
			.and()
				.csrf().disable()
			.build();
	}
}
