package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.validation.RetypePasswordValidator;
import com.naturalprogrammer.spring.lemon.commonsweb.LemonCommonsWebAutoConfiguration;
import com.naturalprogrammer.spring.lemon.commonsweb.security.JwtAuthenticationProvider;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonWebSecurityConfig;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.LemonAuditorAware;
import com.naturalprogrammer.spring.lemon.security.JpaJwtAuthenticationProvider;
import com.naturalprogrammer.spring.lemon.security.LemonAuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemon.security.LemonJpaSecurityConfig;
import com.naturalprogrammer.spring.lemon.security.LemonOAuth2UserService;
import com.naturalprogrammer.spring.lemon.security.LemonOidcUserService;
import com.naturalprogrammer.spring.lemon.security.LemonUserDetailsService;
import com.naturalprogrammer.spring.lemon.security.OAuth2AuthenticationFailureHandler;
import com.naturalprogrammer.spring.lemon.security.OAuth2AuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.UniqueEmailValidator;

/**
 * Spring Lemon Auto Configuration
 * 
 * @author Sanjay Patel
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@AutoConfigureBefore({
	WebMvcAutoConfiguration.class,
	ErrorMvcAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	SecurityFilterAutoConfiguration.class,
	LemonCommonsWebAutoConfiguration.class})
public class LemonAutoConfiguration {
	
	private static final Log log = LogFactory.getLog(LemonAutoConfiguration.class);
	
	public LemonAutoConfiguration() {
		log.info("Created");
	}

	/**
	 * Configures an Auditor Aware if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
	AuditorAware<U> auditorAware(LemonService<U,ID> lemonService) {
		
        log.info("Configuring LemonAuditorAware");       
		return new LemonAuditorAware<U, ID>(lemonService);
	}

	/**
	 * Configures AuthenticationSuccessHandler if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonAuthenticationSuccessHandler.class)
	public LemonAuthenticationSuccessHandler authenticationSuccessHandler(
			ObjectMapper objectMapper, LemonService<?, ?> lemonService, LemonProperties properties) {
		
        log.info("Configuring AuthenticationSuccessHandler");       
		return new LemonAuthenticationSuccessHandler(objectMapper, lemonService, properties);
	}
	
	/**
	 * Configures OAuth2AuthenticationSuccessHandler if missing
	 */
	@Bean
	@ConditionalOnMissingBean(OAuth2AuthenticationSuccessHandler.class)
	public OAuth2AuthenticationSuccessHandler<?> oauth2AuthenticationSuccessHandler(
			LemonProperties properties, JwtService jwtService) {
		
        log.info("Configuring OAuth2AuthenticationSuccessHandler");       
		return new OAuth2AuthenticationSuccessHandler<>(properties, jwtService);
	}
	
	/**
	 * Configures OAuth2AuthenticationFailureHandler if missing
	 */
	@Bean
	@ConditionalOnMissingBean(OAuth2AuthenticationFailureHandler.class)
	public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
		
        log.info("Configuring OAuth2AuthenticationFailureHandler");       
		return new OAuth2AuthenticationFailureHandler();
	}

	/**
	 * Configures AuthenticationFailureHandler if missing
	 */
	@Bean
	@ConditionalOnMissingBean(AuthenticationFailureHandler.class)
    public AuthenticationFailureHandler authenticationFailureHandler() {
		
        log.info("Configuring SimpleUrlAuthenticationFailureHandler");       
    	return new SimpleUrlAuthenticationFailureHandler();
    }	

	/**
	 * Configures UserDetailsService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
	UserDetailsService userDetailService(AbstractUserRepository<U, ID> userRepository) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new LemonUserDetailsService<U, ID>(userRepository);
	}

	/**
	 * Configures LemonOidcUserService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonOidcUserService.class)	
	public LemonOidcUserService lemonOidcUserService(LemonOAuth2UserService<?, ?> lemonOAuth2UserService) {
		
        log.info("Configuring LemonOidcUserService");       
		return new LemonOidcUserService(lemonOAuth2UserService);
	}

	/**
	 * Configures LemonOAuth2UserService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonOAuth2UserService.class)	
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
		LemonOAuth2UserService<U,ID> lemonOAuth2UserService(
			LemonUserDetailsService<U, ?> userDetailsService,
			LemonService<U, ?> lemonService,
			PasswordEncoder passwordEncoder) {
		
        log.info("Configuring LemonOAuth2UserService");       
		return new LemonOAuth2UserService<U,ID>(userDetailsService, lemonService, passwordEncoder);
	}

	/**
	 * Configures JwtAuthenticationProvider if missing
	 */
	@Bean
	@ConditionalOnMissingBean(JwtAuthenticationProvider.class)	
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
			JwtAuthenticationProvider jwtAuthenticationProvider(
			JwtService jwtService,
			LemonUserDetailsService<U, ID> userDetailsService) {
		
        log.info("Configuring JpaJwtAuthenticationProvider");       
		return new JpaJwtAuthenticationProvider<U,ID>(jwtService, userDetailsService);
	}	
	
	/**
	 * Configures LemonSecurityConfig if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonWebSecurityConfig.class)	
	public LemonWebSecurityConfig lemonSecurityConfig() {
		
        log.info("Configuring LemonJpaSecurityConfig");       
		return new LemonJpaSecurityConfig();
	}
	
	/**
	 * Configures LemonUtils
	 */
	@Bean
	public LemonUtils lemonUtils(ApplicationContext applicationContext,
			ObjectMapper objectMapper) {

        log.info("Configuring LemonUtils");       		
		return new LemonUtils();
	}
	
	/**
	 * Configures RetypePasswordValidator if missing
	 */
	@Bean
	@ConditionalOnMissingBean(RetypePasswordValidator.class)
	public RetypePasswordValidator retypePasswordValidator() {
		
        log.info("Configuring RetypePasswordValidator");       
		return new RetypePasswordValidator();
	}
	
	/**
	 * Configures UniqueEmailValidator if missing
	 */
	@Bean
	public UniqueEmailValidator uniqueEmailValidator(AbstractUserRepository<?, ?> userRepository) {
		
        log.info("Configuring UniqueEmailValidator");       
		return new UniqueEmailValidator(userRepository);		
	}
	
}
