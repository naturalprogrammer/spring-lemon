package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.MimeType;
import org.springframework.web.client.RestTemplate;

import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

/**
 * Logs in or registers a user after OAuth2 SignIn/Up
 */
public class LemonOAuth2UserService<U extends AbstractUser<ID>, ID extends Serializable> extends DefaultOAuth2UserService {

	private static final Log log = LogFactory.getLog(LemonOAuth2UserService.class);

	private LemonUserDetailsService<U, ?> userDetailsService;
	private LemonService<U, ?> lemonService;
	private PasswordEncoder passwordEncoder;

	public LemonOAuth2UserService(
			LemonUserDetailsService<U, ?> userDetailsService,
			LemonService<U, ?> lemonService,
			PasswordEncoder passwordEncoder) {

		this.userDetailsService = userDetailsService;
		this.lemonService = lemonService;
		this.passwordEncoder = passwordEncoder;
		
		replaceRestOperarions();
		log.info("Created");
	}

	protected void replaceRestOperarions() {
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		restTemplate.setMessageConverters(makeMessageConverters());
		setRestOperations(restTemplate);
		
		log.info("Rest Operations replaced");
	}

	protected List<HttpMessageConverter<?>> makeMessageConverters() {
		
		log.info("Making message converters");

		MappingJackson2HttpMessageConverter converter =	new MappingJackson2HttpMessageConverter();

        List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.asMediaType(new MimeType("text", "javascript", StandardCharsets.UTF_8))); // Facebook returns text/javascript        

        converter.setSupportedMediaTypes(mediaTypes);
        return Collections.singletonList(converter);
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		OAuth2User oath2User = super.loadUser(userRequest);
		return buildPrincipal(oath2User, userRequest.getClientRegistration().getRegistrationId());
	}

	/**
	 * Builds the security principal from the given userReqest.
	 * Registers the user if not already reqistered
	 */
	public LemonPrincipal buildPrincipal(OAuth2User oath2User, String registrationId) {
		
		Map<String, Object> attributes = oath2User.getAttributes();
		String email = lemonService.getOAuth2Email(registrationId, attributes);
		LexUtils.validate(email != null, "com.naturalprogrammer.spring.oauth2EmailNeeded", registrationId).go();
		
		boolean emailVerified = lemonService.getOAuth2AccountVerified(registrationId, attributes);
		LexUtils.validate(emailVerified, "com.naturalprogrammer.spring.oauth2EmailNotVerified", registrationId).go();
		
    	U user = userDetailsService.findUserByUsername(email).orElseGet(()  -> {
    		
			// register a new user
			U newUser = lemonService.newUser();
			newUser.setEmail(email);
			newUser.setPassword(passwordEncoder.encode(LecUtils.uid()));
			
			lemonService.fillAdditionalFields(registrationId, newUser, attributes);
			lemonService.save(newUser);

			try {
				
				lemonService.mailForgotPasswordLink(newUser);
				
			} catch (Throwable e) {
				
				// In case of exception, just log the error and keep silent			
				log.error(ExceptionUtils.getStackTrace(e));
			}
			
			return newUser;
    	});
    	
    	UserDto userDto = user.toUserDto();
		LemonPrincipal principal = new LemonPrincipal(userDto);
		principal.setAttributes(attributes);
		principal.setName(oath2User.getName());
		
		return principal;
	}
}
