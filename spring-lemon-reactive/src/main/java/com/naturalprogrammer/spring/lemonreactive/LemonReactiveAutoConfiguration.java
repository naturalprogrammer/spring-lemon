package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commonsreactive.LemonCommonsReactiveAutoConfiguration;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import com.naturalprogrammer.spring.lemonreactive.exceptions.handlers.VersionExceptionHandler;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveSecurityConfig;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveUserDetailsService;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;

@Configuration
@EnableMongoAuditing
@EnableReactiveMethodSecurity
@AutoConfigureBefore({
	MongoReactiveAutoConfiguration.class,
	ReactiveUserDetailsServiceAutoConfiguration.class,
	LemonCommonsReactiveAutoConfiguration.class})
@ComponentScan(basePackageClasses=VersionExceptionHandler.class)
public class LemonReactiveAutoConfiguration {
	
	private static final Log log = LogFactory.getLog(LemonReactiveAutoConfiguration.class);
	
	public LemonReactiveAutoConfiguration() {
		log.info("Created");
	}

	
	@Bean
	@ConditionalOnMissingBean(LemonReactiveSecurityConfig.class)
	public <U extends AbstractMongoUser<ID>, ID extends Serializable>
		LemonReactiveSecurityConfig<U,ID> lemonReactiveSecurityConfig(
				JwtService jwtService,
				LemonReactiveUserDetailsService<U, ID> userDetailsService) {
		
		log.info("Configuring LemonReactiveSecurityConfig ...");

		return new LemonReactiveSecurityConfig<U,ID>(jwtService, userDetailsService);
	}
	
	
	/**
	 * Configures UserDetailsService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	public <U extends AbstractMongoUser<ID>, ID extends Serializable>
	LemonReactiveUserDetailsService<U, ID> userDetailService(AbstractMongoUserRepository<U, ID> userRepository) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new LemonReactiveUserDetailsService<U, ID>(userRepository);
	}


	/**
	 * Configures LeeUtils
	 */
	@Bean
	public LerUtils lerUtils(LexUtils lexUtils) {

        log.info("Configuring LerUtils");
		return new LerUtils();
	}
}
