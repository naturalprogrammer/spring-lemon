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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.LemonAuditorAware;
import com.naturalprogrammer.spring.lemon.exceptions.DefaultExceptionHandlerControllerAdvice;
import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponseComposer;
import com.naturalprogrammer.spring.lemon.exceptions.LemonErrorAttributes;
import com.naturalprogrammer.spring.lemon.exceptions.LemonErrorController;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.mail.MockMailSender;
import com.naturalprogrammer.spring.lemon.mail.SmtpMailSender;
import com.naturalprogrammer.spring.lemon.security.AuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemon.security.JwtAuthenticationProvider;
import com.naturalprogrammer.spring.lemon.security.JwtService;
import com.naturalprogrammer.spring.lemon.security.LemonCorsFilter;
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
import com.nimbusds.jose.KeyLengthException;

/**
 * Spring Lemon Auto Configuration
 * 
 * @author Sanjay Patel
 */
@Configuration
@ComponentScan(basePackageClasses=AbstractExceptionHandler.class)
@EnableSpringDataWebSupport
@EnableTransactionManagement
@EnableJpaAuditing
@EnableAsync
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AutoConfigureBefore({
	WebMvcAutoConfiguration.class,
	ErrorMvcAutoConfiguration.class,
	SecurityAutoConfiguration.class,
	SecurityFilterAutoConfiguration.class})
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
	 * Prefixes JSON responses for JSON vulnerability. See for more details:
	 * 
	 * https://docs.angularjs.org/api/ng/service/$http
	 * http://stackoverflow.com/questions/26384930/how-to-add-n-before-each-spring-json-response-to-prevent-common-vulnerab
	 * 
	 * To disable this, in your application.properties, use
	 * lemon.enabled.json-prefix: false
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
	 * Password encoder
	 */
	@Bean
	@ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
	
		log.info("Configuring BCryptPasswordEncoder");		
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
	
	@Bean
	public LemonProperties lemonProperties() {
		
        log.info("Configuring LemonProperties");       
		return new LemonProperties();
	}
	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
	AuditorAware<U> auditorAware(AbstractUserRepository<U,ID> userRepository) {
		
        log.info("Configuring LemonAuditorAware");       
		return new LemonAuditorAware<U, ID>(userRepository);
	}

	@Bean
	@ConditionalOnMissingBean(ErrorResponseComposer.class)
	public <T extends Throwable>
	ErrorResponseComposer<T> errorResponseComposer(List<AbstractExceptionHandler<T>> handlers) {
		
        log.info("Configuring ErrorResponseComposer");       
		return new ErrorResponseComposer<T>(handlers);
	}
	
	@Bean
	@ConditionalOnMissingBean(DefaultExceptionHandlerControllerAdvice.class)
	public <T extends Throwable>
	DefaultExceptionHandlerControllerAdvice<T> defaultExceptionHandlerControllerAdvice(ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring DefaultExceptionHandlerControllerAdvice");       
		return new DefaultExceptionHandlerControllerAdvice<T>(errorResponseComposer);
	}
	
	@Bean
	@ConditionalOnMissingBean(ErrorAttributes.class)
	public <T extends Throwable>
	ErrorAttributes errorAttributes(ErrorResponseComposer<T> errorResponseComposer) {
		
        log.info("Configuring LemonErrorAttributes");       
		return new LemonErrorAttributes<T>(errorResponseComposer);
	}
	
	@Bean
	@ConditionalOnMissingBean(ErrorController.class)
	public ErrorController errorController(ErrorAttributes errorAttributes,
			ServerProperties serverProperties,
			List<ErrorViewResolver> errorViewResolvers) {
		
        log.info("Configuring LemonErrorController");       
		return new LemonErrorController(errorAttributes, serverProperties, errorViewResolvers);	
	}
	
	/**
	 * Configures a MockMailSender when the property
	 * <code>spring.mail.host</code> isn't defined.
	 */
	@Bean
	@ConditionalOnMissingBean(MailSender.class)
	@ConditionalOnProperty(name="spring.mail.host", havingValue="foo", matchIfMissing=true)
	public MailSender mockMailSender() {

        log.info("Configuring MockMailSender");       
        return new MockMailSender();
	}

	
	/**
	 * Configures an SmtpMailSender when the property
	 * <code>spring.mail.host</code> is defined.
	 */
	@Bean
	@ConditionalOnMissingBean(MailSender.class)
	@ConditionalOnProperty("spring.mail.host")
	public MailSender smtpMailSender(JavaMailSender javaMailSender) {
		
        log.info("Configuring SmtpMailSender");       
		return new SmtpMailSender(javaMailSender);
	}
	
	@Bean
	@ConditionalOnMissingBean(JwtService.class)
	public JwtService jwtService(LemonProperties properties) throws KeyLengthException {
		
        log.info("Configuring AuthenticationSuccessHandler");       
		return new JwtService(properties.getJwt().getSecret());
	}

	@Bean
	@ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
	public AuthenticationSuccessHandler authenticationSuccessHandler(
			ObjectMapper objectMapper, JwtService jwtService, LemonProperties properties) {
		
        log.info("Configuring AuthenticationSuccessHandler");       
		return new AuthenticationSuccessHandler(objectMapper, jwtService, properties);
	}
	
	@Bean
	@ConditionalOnMissingBean(OAuth2AuthenticationSuccessHandler.class)
	public OAuth2AuthenticationSuccessHandler<?> oauth2AuthenticationSuccessHandler(
			LemonProperties properties, JwtService jwtService) {
		
        log.info("Configuring OAuth2AuthenticationSuccessHandler");       
		return new OAuth2AuthenticationSuccessHandler<>(properties, jwtService);
	}
	
	@Bean
	@ConditionalOnMissingBean(OAuth2AuthenticationFailureHandler.class)
	public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
		
        log.info("Configuring OAuth2AuthenticationFailureHandler");       
		return new OAuth2AuthenticationFailureHandler();
	}

	/**
	 * Authentication failure handler, to override the default behavior
	 * of spring security -  redirecting to the login screen 
	 */
	@Bean
	@ConditionalOnMissingBean(AuthenticationFailureHandler.class)
    public AuthenticationFailureHandler authenticationFailureHandler() {
		
        log.info("Configuring SimpleUrlAuthenticationFailureHandler");       
    	return new SimpleUrlAuthenticationFailureHandler();
    }	

//	@Bean
//	@ConditionalOnMissingBean(LogoutSuccessHandler.class)
//	public LogoutSuccessHandler logoutSuccessHandler() {
//		
//        log.info("Configuring LemonLogoutSuccessHandler");       
//		return new LemonLogoutSuccessHandler();
//	}
//	
	@Bean
	@ConditionalOnMissingBean(PermissionEvaluator.class)
	public PermissionEvaluator permissionEvaluator() {
		
        log.info("Configuring LemonPermissionEvaluator");       
		return new LemonPermissionEvaluator();
	}

	@Bean
	@ConditionalOnMissingBean(UserDetailsService.class)
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
	UserDetailsService userDetailService(AbstractUserRepository<U, ID> userRepository) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new LemonUserDetailsService<U, ID>(userRepository);
	}

	@Bean
	@ConditionalOnProperty(name="lemon.cors.allowed-origins")
	@ConditionalOnMissingBean(LemonCorsFilter.class)
	public LemonCorsFilter lemonCorsFilter(LemonProperties properties) {
		
        log.info("Configuring LemonCorsFilter");       
		return new LemonCorsFilter(properties);		
	}
	
	@Bean
	@ConditionalOnMissingBean(LemonOidcUserService.class)	
	public LemonOidcUserService lemonOidcUserService(LemonOAuth2UserService<?, ?> lemonOAuth2UserService) {
		
        log.info("Configuring LemonOidcUserService");       
		return new LemonOidcUserService(lemonOAuth2UserService);
	}

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

	@Bean
	@ConditionalOnMissingBean(JwtAuthenticationProvider.class)	
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
		JwtAuthenticationProvider<U,ID> jwtAuthenticationProvider(
			JwtService jwtService,
			LemonUserDetailsService<U, ID> userDetailsService) {
		
        log.info("Configuring LemonSecurityConfig");       
		return new JwtAuthenticationProvider<U,ID>(jwtService, userDetailsService);
	}	
	
	@Bean
	@ConditionalOnMissingBean(LemonSecurityConfig.class)	
	public LemonSecurityConfig lemonSecurityConfig() {
		
        log.info("Configuring LemonSecurityConfig");       
		return new LemonSecurityConfig();
	}
	
	@Bean
	public LemonUtils lemonUtil(ApplicationContext applicationContext,
			MessageSource messageSource, ObjectMapper objectMapper) {

        log.info("Configuring LemonUtil");       		
		return new LemonUtils(applicationContext, messageSource, objectMapper);
	}
	
	@Bean
	@ConditionalOnMissingBean(CaptchaValidator.class)
	public CaptchaValidator captchaValidator(LemonProperties properties, RestTemplateBuilder restTemplateBuilder) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new CaptchaValidator(properties, restTemplateBuilder);
	}
	
	@Bean
	@ConditionalOnMissingBean(RetypePasswordValidator.class)
	public RetypePasswordValidator retypePasswordValidator() {
		
        log.info("Configuring RetypePasswordValidator");       
		return new RetypePasswordValidator();
	}
	
	@Bean
	public UniqueEmailValidator uniqueEmailValidator(AbstractUserRepository<?, ?> userRepository) {
		
        log.info("Configuring UniqueEmailValidator");       
		return new UniqueEmailValidator(userRepository);		
	}
}
