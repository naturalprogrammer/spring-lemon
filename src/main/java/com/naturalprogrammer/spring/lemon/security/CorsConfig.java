package com.naturalprogrammer.spring.lemon.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.naturalprogrammer.spring.lemon.LemonProperties;

@Configuration
@ConditionalOnProperty(name="lemon.allowedOrigins")
public class CorsConfig extends WebMvcConfigurerAdapter {
	
	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	LemonProperties properties;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		
		log.debug("Configuring CORS");
		
		registry.addMapping("/**")
			.allowedOrigins(properties.getAllowedOrigins())
			.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			.allowedHeaders("x-requested-with", "origin", "content-type", "accept", CsrfCookieFilter.XSRF_TOKEN_HEADER_NAME)
			.exposedHeaders("x-requested-with", "origin", "content-type", "accept", CsrfCookieFilter.XSRF_TOKEN_HEADER_NAME)
			.allowCredentials(true).maxAge(3600);
	}
}
