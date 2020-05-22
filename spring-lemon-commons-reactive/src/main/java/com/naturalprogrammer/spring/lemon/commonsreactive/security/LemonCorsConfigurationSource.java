package com.naturalprogrammer.spring.lemon.commonsreactive.security;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties.Cors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;

/**
 * CORS Configuration
 */
public class LemonCorsConfigurationSource implements CorsConfigurationSource {

	private static final Log log = LogFactory.getLog(LemonCorsConfigurationSource.class);

	private Cors cors;

	public LemonCorsConfigurationSource(LemonProperties properties) {

		this.cors = properties.getCors();
		log.info("Created");
	}

	@Override
	public CorsConfiguration getCorsConfiguration(ServerWebExchange exchange) {
		
		CorsConfiguration config = new CorsConfiguration();
		
		config.setAllowCredentials(true);
		config.setAllowedHeaders(Arrays.asList(cors.getAllowedHeaders()));
		config.setAllowedMethods(Arrays.asList(cors.getAllowedMethods()));
		config.setAllowedOrigins(Arrays.asList(cors.getAllowedOrigins()));
		config.setExposedHeaders(Arrays.asList(cors.getExposedHeaders()));
		config.setMaxAge(cors.getMaxAge());
		
		return config;
	}

}
