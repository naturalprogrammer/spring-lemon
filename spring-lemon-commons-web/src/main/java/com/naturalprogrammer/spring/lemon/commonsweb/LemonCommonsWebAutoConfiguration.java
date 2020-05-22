package com.naturalprogrammer.spring.lemon.commonsweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.commons.LemonCommonsAutoConfiguration;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commonsweb.exceptions.DefaultExceptionHandlerControllerAdvice;
import com.naturalprogrammer.spring.lemon.commonsweb.exceptions.LemonErrorAttributes;
import com.naturalprogrammer.spring.lemon.commonsweb.exceptions.LemonErrorController;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonCorsConfigurationSource;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonWebAuditorAware;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonWebSecurityConfig;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.Serializable;
import java.util.List;

@Configuration
@EnableSpringDataWebSupport
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AutoConfigureBefore({
	WebMvcAutoConfiguration.class,
	ErrorMvcAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	SecurityFilterAutoConfiguration.class,
	LemonCommonsAutoConfiguration.class})
public class LemonCommonsWebAutoConfiguration {

	/**
	 * For handling JSON vulnerability,
	 * JSON response bodies would be prefixed with
	 * this string.
	 */
	public static final String JSON_PREFIX = ")]}',\n";

	private static final Log log = LogFactory.getLog(LemonCommonsWebAutoConfiguration.class);
	
	public LemonCommonsWebAutoConfiguration() {
		log.info("Created");
	}
	
    /**
	 * Prefixes JSON responses for JSON vulnerability. Disabled by default.
	 * To enable, add this to your application properties:
	 *     lemon.enabled.json-prefix: true
	 */
	@Bean
	@ConditionalOnProperty(name="lemon.enabled.json-prefix")
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
			ObjectMapper objectMapper) {
		
        log.info("Configuring JSON vulnerability prefix");       

        MappingJackson2HttpMessageConverter converter =
        		new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setJsonPrefix(JSON_PREFIX);
        
        return converter;
	}
	
	/**
	 * Configures DefaultExceptionHandlerControllerAdvice if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(DefaultExceptionHandlerControllerAdvice.class)
	public <T extends Throwable>
	DefaultExceptionHandlerControllerAdvice<T> defaultExceptionHandlerControllerAdvice(
    		ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring DefaultExceptionHandlerControllerAdvice");       
		return new DefaultExceptionHandlerControllerAdvice<T>(errorResponseComposer);
	}
	
	/**
	 * Configures an Error Attributes if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorAttributes.class)
	public <T extends Throwable>
	ErrorAttributes errorAttributes(ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring LemonErrorAttributes");       
		return new LemonErrorAttributes<T>(errorResponseComposer);
	}
	
	/**
	 * Configures an Error Controller if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorController.class)
	public ErrorController errorController(ErrorAttributes errorAttributes,
			ServerProperties serverProperties,
			List<ErrorViewResolver> errorViewResolvers) {
		
        log.info("Configuring LemonErrorController");       
		return new LemonErrorController(errorAttributes, serverProperties, errorViewResolvers);	
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
	
	/**
	 * Configures LemonSecurityConfig if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonWebSecurityConfig.class)	
	public LemonWebSecurityConfig lemonSecurityConfig() {
		
        log.info("Configuring LemonWebSecurityConfig");       
		return new LemonWebSecurityConfig();
	}
	
	/**
	 * Configures an Auditor Aware if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <ID extends Serializable>
	AuditorAware<ID> auditorAware() {
		
        log.info("Configuring LemonAuditorAware");       
		return new LemonWebAuditorAware<ID>();
	}

	/**
	 * Configures LemonUtils
	 */
	@Bean
	public LecwUtils lecwUtils(ApplicationContext applicationContext,
			ObjectMapper objectMapper) {

        log.info("Configuring LecwUtils");       		
		return new LecwUtils();
	}
}
