package com.naturalprogrammer.spring.lemonreactive.security;

import java.io.Serializable;
import java.net.URI;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsreactive.util.LecrUtils;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 * 
 * @author Sanjay Patel
 */
@AllArgsConstructor
public class ReactiveOAuth2AuthenticationSuccessHandler
	implements ServerAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(ReactiveOAuth2AuthenticationSuccessHandler.class);
	private static final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	private BlueTokenService blueTokenService;
	private LemonProperties properties;
	
	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
		Authentication authentication) {
		
		ServerWebExchange exchange = webFilterExchange.getExchange();

		return ReactiveSecurityContextHolder.getContext()
				.cast(OAuth2LoginAuthenticationToken.class)
				.map(this::toLemonPrincipal)
				.map(LemonPrincipal::currentUser)
				.map(this::getAuthToken)
				.map(URI::create)
				.flatMap(location -> redirectStrategy.sendRedirect(exchange, location));
	}
	
	private LemonPrincipal toLemonPrincipal(OAuth2LoginAuthenticationToken token) {
		
		String registrationId = token.getClientRegistration().getRegistrationId();
		OAuth2User principal = token.getPrincipal();
		
		return new LemonPrincipal();
				
	}
	
	private String getAuthToken(UserDto user) {
		
		String shortLivedAuthToken = blueTokenService.createToken(
				BlueTokenService.AUTH_AUDIENCE,
				user.getUsername(),
				(long) properties.getJwt().getShortLivedMillis());
		
		return shortLivedAuthToken;
	}
}
