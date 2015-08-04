package com.naturalprogrammer.spring.lemon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.naturalprogrammer.spring.lemon.mail.MailSender;

@Configuration
@EnableSpringDataWebSupport
@EnableTransactionManagement
@EnableJpaAuditing
@EnableAsync
public class LemonConfig {
	
	public final static String JSON_PREFIX = ")]}',\n";

	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Prefixes JSON responses for JSON vulnerability.
	 * 
	 * To disable this, in your application.properties, use
	 * 
	 * lemon.jsonprefix.enabled: false
	 *
	 * https://docs.angularjs.org/api/ng/service/$http
	 * http://stackoverflow.com/questions/26384930/how-to-add-n-before-each-spring-json-response-to-prevent-common-vulnerab
	 */
	@Bean
	@ConditionalOnProperty(name="lemon.enabled.jsonprefix", matchIfMissing=true)
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setJsonPrefix(JSON_PREFIX);
        
        log.info("Configuring JSON vulnerability prefix ...");
        
        return converter;
	}
	
	/**
	 * Needed in CaptchaValidator.
	 * 
	 * ConditionalOnMissingBean will ensure that this bean will be processed
	 * in the REGISTER_BEAN ConfigurationPhase. For more details see:
	 * ConditionEvaluator.shouldSkip, ConfigurationPhase.REGISTER_BEAN
	 *  
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(RestTemplate.class)
	public RestTemplate restTemplate() {
		
		log.info("Configuring RestTemplate ...");
		
		return new RestTemplate();
	}
	
}
