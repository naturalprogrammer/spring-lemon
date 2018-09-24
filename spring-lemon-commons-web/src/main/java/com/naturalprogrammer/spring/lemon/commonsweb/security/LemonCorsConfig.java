package com.naturalprogrammer.spring.lemon.commonsweb.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties.Cors;

/**
 * CORS Configuration
 */
public class LemonCorsConfig implements WebMvcConfigurer {

	private static final Log log = LogFactory.getLog(LemonCorsConfig.class);

	private Cors cors;
		
	public LemonCorsConfig(LemonProperties properties) {

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
