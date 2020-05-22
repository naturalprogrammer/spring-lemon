package com.naturalprogrammer.spring.lemonreactive;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.domain.IdConverter;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commonsmongo.LemonCommonsMongoAutoConfiguration;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveSecurityConfig;
import com.naturalprogrammer.spring.lemonreactive.security.LemonReactiveUserDetailsService;
import com.naturalprogrammer.spring.lemonreactive.security.ReactiveOAuth2AuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;

@Configuration
@AutoConfigureBefore({
	ReactiveUserDetailsServiceAutoConfiguration.class,
	LemonCommonsMongoAutoConfiguration.class})
public class LemonReactiveAutoConfiguration {
	
	private static final Log log = LogFactory.getLog(LemonReactiveAutoConfiguration.class);
	
	public LemonReactiveAutoConfiguration() {
		log.info("Created");
	}

	@Bean
	@ConditionalOnMissingBean(IdConverter.class)
	public <ID extends Serializable>
	IdConverter<ID> idConverter(LemonReactiveService<?,ID> lemonService) {
		return lemonService::toId;
	}
	
	@Bean
	@ConditionalOnMissingBean(ReactiveOAuth2AuthenticationSuccessHandler.class)
	public <U extends AbstractMongoUser<ID>, ID extends Serializable>
		ReactiveOAuth2AuthenticationSuccessHandler<U, ID> reactiveOAuth2AuthenticationSuccessHandler(
				BlueTokenService blueTokenService,
				AbstractMongoUserRepository<U, ID> userRepository,
				LemonReactiveUserDetailsService<U, ID> userDetailsService,
				LemonReactiveService<U, ID> lemonService,
				PasswordEncoder passwordEncoder,
				LemonProperties properties
) {
		
		log.info("Configuring ReactiveOAuth2AuthenticationSuccessHandler ...");

		return new ReactiveOAuth2AuthenticationSuccessHandler<U,ID>(
				blueTokenService,
				userRepository,
				userDetailsService,
				lemonService,
				passwordEncoder,
				properties);
	}
	
	@Bean
	@ConditionalOnMissingBean(LemonReactiveSecurityConfig.class)
	public <U extends AbstractMongoUser<ID>, ID extends Serializable>
		LemonReactiveSecurityConfig<U,ID> lemonReactiveSecurityConfig(
				BlueTokenService blueTokenService,
				LemonReactiveUserDetailsService<U, ID> userDetailsService,
				ReactiveOAuth2AuthenticationSuccessHandler<U,ID> reactiveOAuth2AuthenticationSuccessHandler,
				LemonProperties properties) {
		
		log.info("Configuring LemonReactiveSecurityConfig ...");

		return new LemonReactiveSecurityConfig<U,ID>(
				blueTokenService,
				userDetailsService,
				reactiveOAuth2AuthenticationSuccessHandler,
				properties);
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
