package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Map;

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
	
	private LemonUserDetailsService<U, ?> userDetailsService;
	private LemonService<U, ?> lemonService;
	private PasswordEncoder passwordEncoder;

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
			fillAdditionalFields(user, attributes);
			
			lemonService.forgotPassword(user);
			user.decorate(user);
		}
		
		LemonUser<PK> lemonUser = new LemonUser<PK>();
		lemonUser.setAttributes(oidcUser.getAttributes());
		lemonUser.setClaims(oidcUser.getClaims());
		lemonUser.setAuthorities(user.getAuthorities());
		lemonUser.setEmail(email);
		lemonUser.setIdToken(oidcUser.getIdToken());
		lemonUser.setName(oidcUser.getName());
		lemonUser.setUserInfo(oidcUser.getUserInfo());
		
		lemonUser.setUserId(user.getId());
		
		return lemonUser;
	}

	private void fillAdditionalFields(U user, Map<String, Object> attributes) {
		// TODO Auto-generated method stub
		
	}
}
