package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Logs in or registers a user after OAuth2 SignIn/Up
 */
public class LemonOAuth2UserService<U extends AbstractUser<U,ID>, ID extends Serializable> extends DefaultOAuth2UserService {

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
		
		log.info("Created");
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
	public LemonPrincipal<ID> buildPrincipal(OAuth2User oath2User, String registrationId) {
		
		Map<String, Object> attributes = oath2User.getAttributes();
		String email = lemonService.getOAuth2Email(registrationId, attributes);
		LexUtils.validate(email != null, "com.naturalprogrammer.spring.oauth2EmailNeeded", registrationId).go();
		
		boolean emailVerified = lemonService.getOAuth2AccountVerified(registrationId, attributes);
		LexUtils.validate(emailVerified, "com.naturalprogrammer.spring.oauth2EmailNotVerified", registrationId).go();
		
    	U user = userDetailsService.findUserByUsername(email).orElseGet(()  -> {
    		
			// register a new user
			U newUser = lemonService.newUser();
			newUser.setEmail(email);
			newUser.setPassword(passwordEncoder.encode(LemonUtils.uid()));
			
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
    	
    	UserDto<ID> userDto = user.toUserDto();
		LemonPrincipal<ID> principal = new LemonPrincipal<>(userDto);
		principal.setAttributes(attributes);
		principal.setName(oath2User.getName());
		
		return principal;
	}
}
