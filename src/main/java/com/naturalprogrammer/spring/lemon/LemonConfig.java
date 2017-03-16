package com.naturalprogrammer.spring.lemon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Although most of the configurations are
 * inside various sub-packages, some didn't fit
 * anywhere, which are here, inside the root package. 
 * 
 * @author Sanjay Patel
 */
@Configuration
@ComponentScan
@EnableSpringDataWebSupport
@EnableTransactionManagement
@EnableJpaAuditing
@EnableAsync
@AutoConfigureBefore({WebMvcAutoConfiguration.class})
public class LemonConfig {
	
	/**
	 * For handling JSON vulnerability,
	 * JSON response bodies would be prefixed with
	 * this string.
	 */
	public final static String JSON_PREFIX = ")]}',\n";

	private static final Log log = LogFactory.getLog(LemonConfig.class);
	
	public LemonConfig() {
		log.info("Created");
	}

	/**
	 * Prefixes JSON responses for JSON vulnerability. See for more details:
	 * 
	 * https://docs.angularjs.org/api/ng/service/$http
	 * http://stackoverflow.com/questions/26384930/how-to-add-n-before-each-spring-json-response-to-prevent-common-vulnerab
	 * 
	 * To disable this, in your application.properties, use
	 * lemon.enabled.json-prefix: false
	 */
	@Bean
	@ConditionalOnProperty(name="lemon.enabled.json-prefix", matchIfMissing=true)
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		
        log.info("Configuring JSON vulnerability prefix ...");       

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setJsonPrefix(JSON_PREFIX);
        
        return converter;
	}
	
	/**
	 * Password encoder
	 */
	@Bean
    public PasswordEncoder passwordEncoder() {
	
		log.info("Configuring Password Encoder ...");		
        return new BCryptPasswordEncoder();
    }
}
