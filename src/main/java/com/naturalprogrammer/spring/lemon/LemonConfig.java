package com.naturalprogrammer.spring.lemon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableSpringDataWebSupport
@EnableTransactionManagement
@EnableJpaAuditing
@EnableAsync
public class LemonConfig {
	
	private final Log log = LogFactory.getLog(getClass());

	@Bean
	public RestTemplate restTemplate() {
		
		log.info("Configuring RestTemplate ...");
		
		return new RestTemplate();
	}
	
}
