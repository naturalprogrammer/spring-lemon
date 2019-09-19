package com.naturalprogrammer.spring.lemon.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.commons.exceptions.handlers.BadCredentialsExceptionHandler;
import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemon.commons.mail.MockMailSender;
import com.naturalprogrammer.spring.lemon.commons.mail.SmtpMailSender;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonJweService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonJwsService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPermissionEvaluator;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.validation.CaptchaValidator;
import com.naturalprogrammer.spring.lemon.exceptions.LemonExceptionsAutoConfiguration;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;

@Configuration
@ComponentScan(basePackageClasses=BadCredentialsExceptionHandler.class)
@EnableAsync
@AutoConfigureBefore({
	LemonExceptionsAutoConfiguration.class})
public class LemonCommonsAutoConfiguration {

	private static final Log log = LogFactory.getLog(LemonCommonsAutoConfiguration.class);
	
	public LemonCommonsAutoConfiguration() {
		log.info("Created");
	}
	
	
	/**
	 * Spring Lemon related properties
	 */	
	@Bean
	public LemonProperties lemonProperties() {
		
        log.info("Configuring LemonProperties");       
		return new LemonProperties();
	}
	

	/**
	 * Configures AuthTokenService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(BlueTokenService.class)
	public BlueTokenService blueTokenService(LemonProperties properties) throws JOSEException {
		
        log.info("Configuring AuthTokenService");       
		return new LemonJwsService(properties.getJwt().getSecret());
	}


	/**
	 * Configures ExternalTokenService if missing
	 */
	@Bean
	@ConditionalOnMissingBean(GreenTokenService.class)
	public GreenTokenService greenTokenService(LemonProperties properties) throws KeyLengthException {
		
        log.info("Configuring ExternalTokenService");       
		return new LemonJweService(properties.getJwt().getSecret());
	}


	/**
	 * Configures Password encoder if missing
	 */
	@Bean
	@ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
	
		log.info("Configuring PasswordEncoder");		
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
	 * Configures a MockMailSender when the property
	 * <code>spring.mail.host</code> isn't defined.
	 */
	@Bean
	@ConditionalOnMissingBean(MailSender.class)
	@ConditionalOnProperty(name="spring.mail.host", havingValue="foo", matchIfMissing=true)
	public MailSender<?> mockMailSender() {

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
	public MailSender<?> smtpMailSender(JavaMailSender javaMailSender) {
		
        log.info("Configuring SmtpMailSender");       
		return new SmtpMailSender(javaMailSender);
	}
	
	@Bean
	public LecUtils lecUtils(ApplicationContext applicationContext, ObjectMapper objectMapper) {
		return new LecUtils(applicationContext, objectMapper);
	}
	
	/**
	 * Configures CaptchaValidator if missing
	 */
	@Bean
	@ConditionalOnMissingBean(CaptchaValidator.class)
	public CaptchaValidator captchaValidator(LemonProperties properties) {
		
        log.info("Configuring LemonUserDetailsService");       
		return new CaptchaValidator(properties);
	}
}
