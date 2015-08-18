package com.naturalprogrammer.spring.lemon.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.LemonProperties.Cors;

@Configuration
@ConditionalOnProperty(name="lemon.cors.allowedOrigins")
public class CorsConfig extends WebMvcConfigurerAdapter {
	
	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	LemonProperties properties;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		
		log.debug("Configuring CORS");
		
		Cors cors = properties.getCors();
		
		registry.addMapping("/**")
			.allowedOrigins(cors.getAllowedOrigins())
			.allowedMethods(cors.getAllowedMethods())
			.allowedHeaders(cors.getAllowedHeaders())
			.exposedHeaders(cors.getExposedHeaders())
			.maxAge(cors.getMaxAge())
			.allowCredentials(true);
	}
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
		
		return new CorsFilter(corsConfigurationSource);
		
	}

}
