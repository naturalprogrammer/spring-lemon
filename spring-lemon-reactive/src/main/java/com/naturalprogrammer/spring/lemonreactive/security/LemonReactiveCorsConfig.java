package com.naturalprogrammer.spring.lemonreactive.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties.Cors;

/**
 * CORS Configuration
 */
public class LemonReactiveCorsConfig implements WebFluxConfigurer {

	private static final Log log = LogFactory.getLog(LemonReactiveCorsConfig.class);

	private Cors cors;
		
	public LemonReactiveCorsConfig(LemonProperties properties) {

		this.cors = properties.getCors();
		log.info("Created");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		
        registry.addMapping("/**")
	        .allowedOrigins(cors.getAllowedOrigins())
	        .allowedMethods(cors.getAllowedMethods())
	        .allowedHeaders(cors.getAllowedHeaders())
	        .exposedHeaders(cors.getExposedHeaders())
	        .allowCredentials(true)
	        .maxAge(cors.getMaxAge());
	}
}
