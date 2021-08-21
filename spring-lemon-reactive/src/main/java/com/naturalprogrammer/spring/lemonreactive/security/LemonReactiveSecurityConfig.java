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
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsreactive.security.LemonCommonsReactiveSecurityConfig;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.io.Serializable;

public class LemonReactiveSecurityConfig<U extends AbstractMongoUser<ID>, ID extends Serializable> extends LemonCommonsReactiveSecurityConfig {

	private static final Log log = LogFactory.getLog(LemonReactiveSecurityConfig.class);
	
	protected LemonReactiveUserDetailsService<U, ID> userDetailsService;
	private LemonProperties properties;
	private ReactiveOAuth2AuthenticationSuccessHandler<U,ID> reactiveOAuth2AuthenticationSuccessHandler;

	public LemonReactiveSecurityConfig(BlueTokenService blueTokenService,
			LemonReactiveUserDetailsService<U, ID> userDetailsService,
			ReactiveOAuth2AuthenticationSuccessHandler<U,ID> reactiveOAuth2AuthenticationSuccessHandler,
			LemonProperties properties) {
		
		super(blueTokenService);
		this.userDetailsService = userDetailsService;
		this.reactiveOAuth2AuthenticationSuccessHandler = reactiveOAuth2AuthenticationSuccessHandler;
		this.properties = properties;
		
		log.info("Created");
	}

	/**
	 * Configure form login
	 */
	@Override
	protected void formLogin(ServerHttpSecurity http) {
		
		http.formLogin()
			.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
			.loginPage(loginPage()) // Should be "/login" by default, but not providing that overwrites our AuthenticationFailureHandler, because this is called later 
			.authenticationFailureHandler((exchange, exception) -> Mono.error(exception))
			.authenticationSuccessHandler(new WebFilterChainServerAuthenticationSuccessHandler());
	}

	/**
	 * Override this to change login URL
	 */
	protected String loginPage() {
		
		return "/api/core/login";
	}

	/**
	 * Configure OAuth2 login
	 */
	@Override
	protected void oauth2Login(ServerHttpSecurity http) {

		http.oauth2Login()
			.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
			.authorizedClientRepository(new ReactiveCookieServerOAuth2AuthorizedClientRepository(properties))
			.authenticationSuccessHandler(reactiveOAuth2AuthenticationSuccessHandler)
			.authenticationFailureHandler(this::onOauth2AuthenticationFailure);
	}
	
	@Override
	protected Mono<UserDto> fetchUserDto(JWTClaimsSet claims) {
		
		String username = claims.getSubject();
		
		return userDetailsService.findUserByUsername(username)
			.switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException(username))))
			.doOnNext(user -> {
		        log.debug("User found ...");
		        LerUtils.ensureCredentialsUpToDate(claims, user);
			})
			.map(AbstractMongoUser::toUserDto);
	}
	
	protected Mono<Void> onOauth2AuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
		
		ReactiveCookieServerOAuth2AuthorizedClientRepository.deleteCookies(webFilterExchange.getExchange(),
				LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME,
				LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
		
		return Mono.error(exception);
	}
}
