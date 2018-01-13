package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

public class LemonOidcUserService<U extends AbstractUser<U,PK>, PK extends Serializable> extends OidcUserService {
	
	private static final Log log = LogFactory.getLog(LemonOidcUserService.class);

	private LemonUserDetailsService<U, ?> userDetailsService;
	private LemonService<U, ?> lemonService;
	private PasswordEncoder passwordEncoder;

	public LemonOidcUserService(
			LemonUserDetailsService<U, ?> userDetailsService,
			LemonService<U, ?> lemonService,
			PasswordEncoder passwordEncoder) {

		this.userDetailsService = userDetailsService;
		this.lemonService = lemonService;
		this.passwordEncoder = passwordEncoder;
		
		log.info("Created");
	}

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		
		OidcUser oidcUser = super.loadUser(userRequest);
		
		Map<String, Object> attributes = oidcUser.getAttributes();
		String email = (String) attributes.get(StandardClaimNames.EMAIL);
		
    	U user;
    	
		try {
			
			// Return the user if it already exists
			user = userDetailsService.loadUserByUsername(email);
			
		} catch (UsernameNotFoundException e) {
			
			// register a new user
			user = lemonService.newUser();
			user.setEmail(email);
			user.setUsername(user.getEmail());
			user.setPassword(passwordEncoder.encode(LemonUtils.uid()));
			
			lemonService.fillAdditionalFields(user, attributes);
			
			lemonService.forgotPassword(user);
			user.decorate(user);
		}
		
		user.setAttributes(oidcUser.getAttributes());
		user.setClaims(oidcUser.getClaims());
		user.setIdToken(oidcUser.getIdToken());
		user.setUserInfo(oidcUser.getUserInfo());
		
		return user;
	}
}
