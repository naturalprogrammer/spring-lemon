package com.naturalprogrammer.spring.lemon;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.domain.LemonAuditorAware;
import com.naturalprogrammer.spring.lemon.exceptions.LemonErrorAttributes;
import com.naturalprogrammer.spring.lemon.exceptions.LemonErrorController;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.LemonExceptionHandler;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.mail.MockMailSender;
import com.naturalprogrammer.spring.lemon.mail.SmtpMailSender;
import com.naturalprogrammer.spring.lemon.security.AuthenticationSuccessHandler;
import com.naturalprogrammer.spring.lemon.security.LemonCorsFilter;
import com.naturalprogrammer.spring.lemon.security.LemonLogoutSuccessHandler;
import com.naturalprogrammer.spring.lemon.security.LemonOidcUserService;
import com.naturalprogrammer.spring.lemon.security.LemonPermissionEvaluator;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;
import com.naturalprogrammer.spring.lemon.security.LemonTokenAuthenticationFilter;
import com.naturalprogrammer.spring.lemon.security.LemonUserDetailsService;
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
@ComponentScan(basePackageClasses=LemonExceptionHandler.class)
@EnableSpringDataWebSupport
@EnableTransactionManagement
@EnableJpaAuditing
@EnableAsync
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AutoConfigureBefore({WebMvcAutoConfiguration.class,
	ErrorMvcAutoConfiguration.class,
	SecurityAutoConfiguration.class})
public class LemonAutoConfiguration {
	
	// remember-me related constants
	public static final String REMEMBER_ME_COOKIE = "rememberMe";
	public static final String REMEMBER_ME_PARAMETER = "rememberMe";
	
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
	@ConditionalOnProperty(name="lemon.enabled.json-prefix", matchIfMissing=true)
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
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public LemonProperties lemonProperties() {
		
        log.info("Configuring LemonProperties");       
		return new LemonProperties();
	}
	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
	AuditorAware<U> auditorAware() {
		
        log.info("Configuring LemonAuditorAware");       
		return new LemonAuditorAware<U, ID>();
	}
	
//	@Bean
//	@ConditionalOnMissingBean(ErrorAttributes.class)
//	public ErrorAttributes errorAttributes(List<LemonExceptionHandler<?>> handlers) {
//		
//        log.info("Configuring LemonErrorAttributes");       
//		return new LemonErrorAttributes(handlers);
//	}
	
//	@Bean
//	@ConditionalOnMissingBean(ErrorController.class)
//	public ErrorController errorController(ErrorAttributes errorAttributes,
//			ServerProperties serverProperties,
//			List<ErrorViewResolver> errorViewResolvers) {
//		
//        log.info("Configuring LemonErrorController");       
//		return new LemonErrorController(errorAttributes, serverProperties, errorViewResolvers);	
//	}
	
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
	@ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
	public AuthenticationSuccessHandler authenticationSuccessHandler(
			ObjectMapper objectMapper, @Lazy LemonService<?, ?> lemonService) {
		
        log.info("Configuring AuthenticationSuccessHandler");       
		return new AuthenticationSuccessHandler(objectMapper, lemonService);
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

	@Bean
	@ConditionalOnMissingBean(LogoutSuccessHandler.class)
	public LogoutSuccessHandler logoutSuccessHandler() {
		
        log.info("Configuring LemonLogoutSuccessHandler");       
		return new LemonLogoutSuccessHandler();
	}
	
	@Bean
	@ConditionalOnMissingBean(PermissionEvaluator.class)
	public PermissionEvaluator permissionEvaluator() {
		
        log.info("Configuring LemonPermissionEvaluator");       
		return new LemonPermissionEvaluator();
	}

	/**
	 * Override this method if you want to 
	 * setup a different RememberMeServices
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(RememberMeServices.class)
	public RememberMeServices rememberMeServices(LemonProperties properties, UserDetailsService userDetailsService) {
    	
        log.info("Configuring TokenBasedRememberMeServices");       

        TokenBasedRememberMeServices rememberMeServices =
        	new TokenBasedRememberMeServices
        		(properties.getRememberMeKey(), userDetailsService);
        rememberMeServices.setParameter(REMEMBER_ME_PARAMETER); // default is "remember-me" (in earlier spring security versions it was "_spring_security_remember_me")
        rememberMeServices.setCookieName(REMEMBER_ME_COOKIE);
        return rememberMeServices;       
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
	@ConditionalOnMissingBean(LemonTokenAuthenticationFilter.class)	
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
		LemonTokenAuthenticationFilter<U,ID> lemonTokenAuthenticationFilter(
			PasswordEncoder passwordEncoder,
			AbstractUserRepository<U, ID> userRepository,
			LemonService<U,ID> lemonService) {
		
        log.info("Configuring LemonTokenAuthenticationFilter");       
		return new LemonTokenAuthenticationFilter<U, ID>
			(passwordEncoder, userRepository, lemonService);
	}
	
	@Bean
	@ConditionalOnMissingBean(LemonOidcUserService.class)	
	public <U extends AbstractUser<U,ID>, ID extends Serializable>
		LemonOidcUserService<U,ID> lemonOidcUserService(
			LemonUserDetailsService<U, ?> userDetailsService,
			LemonService<U, ?> lemonService,
			PasswordEncoder passwordEncoder) {
		
        log.info("Configuring LemonOidcUserService");       
		return new LemonOidcUserService<U,ID>
			(userDetailsService, lemonService, passwordEncoder);
	}

//	@Bean
//	@ConditionalOnMissingBean(DefaultPrincipalExtractor.class)
//	public <U extends AbstractUser<U,?>>
//	DefaultPrincipalExtractor<U> defaultPrincipalExtractor() {
//		
//        log.info("Configuring DefaultPrincipalExtractor");       
//		return new DefaultPrincipalExtractor<U>();
//	}
//	
	@Bean
	@ConditionalOnMissingBean(LemonSecurityConfig.class)	
	public LemonSecurityConfig lemonSecurityConfig() {
		
        log.info("Configuring LemonSecurityConfig");       
		return new LemonSecurityConfig();
	}
	
/*
 * 	@Bean
	@ConditionalOnMissingBean
	public OAuth2AuthorizedClientService authorizedClientService(
			ClientRegistrationRepository clientRegistrationRepository) {
		
        log.info("Configuring OAuth2AuthorizedClientService");       
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}
*/
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
