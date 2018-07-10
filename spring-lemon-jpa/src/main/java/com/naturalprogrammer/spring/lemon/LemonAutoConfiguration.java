package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemon.commons.mail.MockMailSender;
import com.naturalprogrammer.spring.lemon.commons.mail.SmtpMailSender;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.LemonAuditorAware;
import com.naturalprogrammer.spring.lemon.exceptions.DefaultExceptionHandlerControllerAdvice;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.LemonErrorAttributes;
import com.naturalprogrammer.spring.lemon.exceptions.LemonErrorController;
import com.naturalprogrammer.spring.lemon.exceptions.LemonExceptionsAutoConfiguration;
import com.naturalprogrammer.spring.lemon.security.AuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemon.security.JwtAuthenticationProvider;
import com.naturalprogrammer.spring.lemon.security.LemonCorsConfig;
import com.naturalprogrammer.spring.lemon.security.LemonOAuth2UserService;
import com.naturalprogrammer.spring.lemon.security.LemonOidcUserService;
import com.naturalprogrammer.spring.lemon.security.LemonPermissionEvaluator;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.security.LemonUserDetailsService;
import com.naturalprogrammer.spring.lemon.security.OAuth2AuthenticationFailureHandler;
import com.naturalprogrammer.spring.lemon.security.OAuth2AuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemon.validation.CaptchaValidator;
import com.naturalprogrammer.spring.lemon.validation.RetypePasswordValidator;
import com.naturalprogrammer.spring.lemon.validation.UniqueEmailValidator;

/**
 * Spring Lemon Auto Configuration
 * 
 * @author Sanjay Patel
 */
@Configuration
@EnableSpringDataWebSupport
@EnableTransactionManagement
@EnableJpaAuditing
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AutoConfigureBefore({
	WebMvcAutoConfiguration.class,
	ErrorMvcAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	SecurityFilterAutoConfiguration.class,
	LemonExceptionsAutoConfiguration.class})
public class LemonAutoConfiguration {
	
	/**
	 * For handling JSON vulnerability,
	 * JSON response bodies would be prefixed with
	 * this string.
	 */
	public final static String JSON_PREFIX = ")]}',\n";

	private static final Log log = LogFactory.getLog(LemonAutoConfiguration.class);
	
	public LemonAutoConfiguration() {
		log.info("Created");
	}

	/**
	 * Prefixes JSON responses for JSON vulnerability. Disabled by default.
	 * To enable, add this to your application properties:
	 *     lemon.enabled.json-prefix: true
	 */
	@Bean
	@ConditionalOnProperty(name="lemon.enabled.json-prefix")
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
			ObjectMapper objectMapper) {
		
        log.info("Configuring JSON vulnerability prefix");       

        MappingJackson2HttpMessageConverter converter =
        		new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setJsonPrefix(JSON_PREFIX);
        
        return converter;
	}
	
	/**
	 * Configures an Auditor Aware if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
	AuditorAware<U> auditorAware(AbstractUserRepository<U,ID> userRepository) {
		
        log.info("Configuring LemonAuditorAware");       
		return new LemonAuditorAware<U, ID>(userRepository);
	}

	/**
	 * Configures DefaultExceptionHandlerControllerAdvice if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(DefaultExceptionHandlerControllerAdvice.class)
	public <T extends Throwable>
	DefaultExceptionHandlerControllerAdvice<T> defaultExceptionHandlerControllerAdvice(ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring DefaultExceptionHandlerControllerAdvice");       
		return new DefaultExceptionHandlerControllerAdvice<T>(errorResponseComposer);
	}
	
	/**
	 * Configures an Error Attributes if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorAttributes.class)
	public <T extends Throwable>
	ErrorAttributes errorAttributes(ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring LemonErrorAttributes");       
		return new LemonErrorAttributes<T>(errorResponseComposer);
	}
	
	/**
	 * Configures an Error Controller if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorController.class)
	public ErrorController errorController(ErrorAttributes errorAttributes,
			ServerProperties serverProperties,
			List<ErrorViewResolver> errorViewResolvers) {
		
        log.info("Configuring LemonErrorController");       
		return new LemonErrorController(errorAttributes, serverProperties, errorViewResolvers);	
	}	

	/**
	 * Configures AuthenticationSuccessHandler if missing
	 */
	@Bean
	@ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
	public AuthenticationSuccessHandler authenticationSuccessHandler(
			ObjectMapper objectMapper, LemonService<?, ?> lemonService, LemonProperties properties) {
		
        log.info("Configuring AuthenticationSuccessHandler");       
		return new AuthenticationSuccessHandler(objectMapper, lemonService, properties);
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
	 * Configures PermissionEvaluator if missing
	 */
	@Bean
	@ConditionalOnMissingBean(PermissionEvaluator.class)
	public PermissionEvaluator permissionEvaluator() {
		
        log.info("Configuring LemonPermissionEvaluator");       
		return new LemonPermissionEvaluator();
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
	 * Configures LemonCorsConfig if missing and lemon.cors.allowed-origins is provided
	 */
	@Bean
	@ConditionalOnProperty(name="lemon.cors.allowed-origins")
	@ConditionalOnMissingBean(LemonCorsConfig.class)
	public LemonCorsConfig lemonCorsConfig(LemonProperties properties) {
		
        log.info("Configuring LemonCorsConfig");       
		return new LemonCorsConfig(properties);		
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
		JwtAuthenticationProvider<U,ID> jwtAuthenticationProvider(
			JwtService jwtService,
			LemonUserDetailsService<U, ID> userDetailsService) {
		
        log.info("Configuring JwtAuthenticationProvider");       
		return new JwtAuthenticationProvider<U,ID>(jwtService, userDetailsService);
	}	
	
	/**
	 * Configures LemonSecurityConfig if missing
	 */
	@Bean
	@ConditionalOnMissingBean(LemonSecurityConfig.class)	
	public LemonSecurityConfig lemonSecurityConfig() {
		
        log.info("Configuring LemonSecurityConfig");       
		return new LemonSecurityConfig();
	}
	
	/**
	 * Configures LemonUtils
	 */
	@Bean
	public LemonUtils lemonUtil(ApplicationContext applicationContext,
			ObjectMapper objectMapper) {

        log.info("Configuring LemonUtil");       		
		return new LemonUtils(applicationContext, objectMapper);
	}
	
	/**
	 * Configures CaptchaValidator if missing
	 */
	@Bean
	@ConditionalOnMissingBean(CaptchaValidator.class)
	public CaptchaValidator captchaValidator(LemonProperties properties, RestTemplateBuilder restTemplateBuilder) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new CaptchaValidator(properties, restTemplateBuilder);
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
