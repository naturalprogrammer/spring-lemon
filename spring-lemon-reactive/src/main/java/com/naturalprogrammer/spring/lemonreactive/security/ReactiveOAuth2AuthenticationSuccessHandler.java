/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.spring.lemonreactive.security;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsreactive.util.LecrUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemonreactive.LemonReactiveService;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 * 
 * @author Sanjay Patel
 */
@AllArgsConstructor
public class ReactiveOAuth2AuthenticationSuccessHandler<U extends AbstractMongoUser<ID>, ID extends Serializable>
	implements ServerAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(ReactiveOAuth2AuthenticationSuccessHandler.class);
	private static final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	private BlueTokenService blueTokenService;
	private AbstractMongoUserRepository<U, ID> userRepository;
	private LemonReactiveUserDetailsService<U, ?> userDetailsService;
	private LemonReactiveService<U, ?> lemonService;
	private PasswordEncoder passwordEncoder;
	private LemonProperties properties;
	
	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
		Authentication authentication) {
		
		ServerWebExchange exchange = webFilterExchange.getExchange();

		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.cast(OAuth2AuthenticationToken.class)
				.flatMap(token -> buildPrincipal(token.getPrincipal(), token.getAuthorizedClientRegistrationId()))
				.map(LemonPrincipal::currentUser)
				.map(this::getAuthToken)
				.map(authToken -> getTargetUrl(exchange, authToken))
				.map(URI::create)
				.flatMap(location -> redirectStrategy.sendRedirect(exchange, location));
	}
	
	/**
	 * Builds the security principal from the given userReqest.
	 * Registers the user if not already registered
	 */
	public Mono<LemonPrincipal> buildPrincipal(OAuth2User oath2User, String registrationId) {
		
		Map<String, Object> attributes = oath2User.getAttributes();
		String email = lemonService.getOAuth2Email(registrationId, attributes);
		LexUtils.validate(email != null, "com.naturalprogrammer.spring.oauth2EmailNeeded", registrationId).go();
		
		boolean emailVerified = lemonService.getOAuth2AccountVerified(registrationId, attributes);
		LexUtils.validate(emailVerified, "com.naturalprogrammer.spring.oauth2EmailNotVerified", registrationId).go();
		
		return userDetailsService.findUserByUsername(email)
				.switchIfEmpty(newUser(email, registrationId, attributes))
				.map(U::toUserDto)
				.map(userDto -> {
					
					LemonPrincipal principal = new LemonPrincipal(userDto);
					principal.setAttributes(attributes);
					principal.setName(oath2User.getName());
					
					return principal;
		});
	}
	
	private Mono<U> newUser(String email, String registrationId, Map<String, Object> attributes) {
		
		// register a new user
		U newUser = lemonService.newUser();
		newUser.setEmail(email);
		newUser.setPassword(passwordEncoder.encode(LecUtils.uid()));
		
		lemonService.fillAdditionalFields(registrationId, newUser, attributes);
		return userRepository.insert(newUser).doOnSuccess(user -> {
			try {
				
				lemonService.mailForgotPasswordLink(newUser);
				
			} catch (Exception e) {
				
				// In case of exception, just log the error and keep silent			
				log.error(ExceptionUtils.getStackTrace(e));
			}
		});
	}

	private String getAuthToken(UserDto user) {
		
		return blueTokenService.createToken(
				BlueTokenService.AUTH_AUDIENCE,
				user.getUsername(),
				(long) properties.getJwt().getShortLivedMillis());
	}
	
	private String getTargetUrl(ServerWebExchange exchange, String shortLivedAuthToken) {
		
		String targetUrl = LecrUtils.fetchCookie(exchange,
				LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME)
				.map(HttpCookie::getValue)
				.orElse(properties.getOauth2AuthenticationSuccessUrl());
		
		ReactiveCookieServerOAuth2AuthorizedClientRepository.deleteCookies(exchange,
				LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME,
				LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
		
		return targetUrl + shortLivedAuthToken;
	}
}
