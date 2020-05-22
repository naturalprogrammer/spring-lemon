package com.naturalprogrammer.spring.lemon.commonsreactive;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.naturalprogrammer.spring.lemon.commons.LemonCommonsAutoConfiguration;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commonsreactive.exceptions.LemonReactiveErrorAttributes;
import com.naturalprogrammer.spring.lemon.commonsreactive.exceptions.handlers.VersionExceptionHandler;
import com.naturalprogrammer.spring.lemon.commonsreactive.security.LemonCommonsReactiveSecurityConfig;
import com.naturalprogrammer.spring.lemon.commonsreactive.security.LemonCorsConfigurationSource;
import com.naturalprogrammer.spring.lemon.commonsreactive.security.LemonReactiveAuditorAware;
import com.naturalprogrammer.spring.lemon.commonsreactive.util.LecrUtils;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.io.Serializable;

@Configuration
@EnableReactiveMethodSecurity
@AutoConfigureBefore({
	WebFluxAutoConfiguration.class,
	ErrorWebFluxAutoConfiguration.class,
	ReactiveSecurityAutoConfiguration.class,
	LemonCommonsAutoConfiguration.class})
@ComponentScan(basePackageClasses=VersionExceptionHandler.class)
public class LemonCommonsReactiveAutoConfiguration {
	
	private static final Log log = LogFactory.getLog(LemonCommonsReactiveAutoConfiguration.class);
	
	public LemonCommonsReactiveAutoConfiguration() {
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
	@ConditionalOnMissingBean(LemonCommonsReactiveSecurityConfig.class)
	public LemonCommonsReactiveSecurityConfig lemonReactiveSecurityConfig(BlueTokenService blueTokenService) {
		
		log.info("Configuring LemonCommonsReactiveSecurityConfig ...");
		return new LemonCommonsReactiveSecurityConfig(blueTokenService);
	}
	
	
	/**
	 * Configures SecurityWebFilterChain if missing
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(
			ServerHttpSecurity http,
			LemonCommonsReactiveSecurityConfig securityConfig,
			AbstractSecurityExpressionHandler<?> expressionHandler,
			PermissionEvaluator permissionEvaluator) {
		
		log.info("Configuring SecurityWebFilterChain ...");
		expressionHandler.setPermissionEvaluator(permissionEvaluator);
		return securityConfig.springSecurityFilterChain(http);
	}
	
	
	/**
	 * Configures LemonCorsConfig if missing and lemon.cors.allowed-origins is provided
	 */
	@Bean
	@ConditionalOnProperty(name="lemon.cors.allowed-origins")
	@ConditionalOnMissingBean(CorsConfigurationSource.class)
	public LemonCorsConfigurationSource corsConfigurationSource(LemonProperties properties) {
		
        log.info("Configuring LemonCorsConfigurationSource");       
		return new LemonCorsConfigurationSource(properties);		
	}

	
	@Bean
	public SimpleModule objectIdModule() {
		
		SimpleModule module = new SimpleModule();
		module.addSerializer(ObjectId.class, new ToStringSerializer());
		
		return module;
	}
	
	
	/**
	 * Configures an Auditor Aware if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <ID extends Serializable>
	AuditorAware<ID> auditorAware() {
		
        log.info("Configuring LemonAuditorAware");       
		return new LemonReactiveAuditorAware<ID>();
	}


	/**
	 * Configures LeeUtils
	 */
	@Bean
	public LecrUtils lecrUtils(LexUtils lexUtils) {

        log.info("Configuring LecrUtils");
		return new LecrUtils();
	}
}
