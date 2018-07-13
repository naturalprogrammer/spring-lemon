package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
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
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.LemonExceptionsAutoConfiguration;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import com.naturalprogrammer.spring.lemonreactive.exceptions.LemonReactiveErrorAttributes;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveSecurityConfig;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveUserDetailsService;

@Configuration
@EnableMongoAuditing
@EnableReactiveMethodSecurity
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

	

	
	@Bean
	@ConditionalOnMissingBean(LemonReactiveSecurityConfig.class)
	public <U extends AbstractMongoUser<ID>, ID extends Serializable>
		LemonReactiveSecurityConfig<U,ID> lemonReactiveSecurityConfig(
				JwtService jwtService,
				LemonReactiveUserDetailsService<U, ID> userDetailsService) {
		
		log.info("Configuring LemonReactiveSecurityConfig ...");

		return new LemonReactiveSecurityConfig<U,ID>(jwtService, userDetailsService);
	}
	
	
	/**
	 * Configures SecurityWebFilterChain if missing
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(
			ServerHttpSecurity http,
			LemonReactiveSecurityConfig<?,?> securityConfig) {
		
		log.info("Configuring SecurityWebFilterChain ...");
		
		return securityConfig.springSecurityFilterChain(http);
	}
	
	
	/**
	 * Configures UserDetailsService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	public <U extends AbstractMongoUser<ID>, ID extends Serializable>
	LemonReactiveUserDetailsService<U, ID> userDetailService(AbstractMongoUserRepository<U, ID> userRepository) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new LemonReactiveUserDetailsService<U, ID>(userRepository);
	}

	@Bean
	public SimpleModule objectIdModule() {
		
		SimpleModule module = new SimpleModule();
		module.addSerializer(ObjectId.class, new ToStringSerializer());
		
		return module;
	}
}
