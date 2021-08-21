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
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsreactive.util.LecrUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class ReactiveCookieServerOAuth2AuthorizedClientRepository implements ServerOAuth2AuthorizedClientRepository {

	private static final Log log = LogFactory.getLog(ReactiveCookieServerOAuth2AuthorizedClientRepository.class);

	private int cookieExpirySecs;
	
	public ReactiveCookieServerOAuth2AuthorizedClientRepository(LemonProperties properties) {

		cookieExpirySecs = properties.getJwt().getShortLivedMillis() / 1000;
	}

	@Override
	public Mono<OAuth2AuthorizedClient> loadAuthorizedClient(String clientRegistrationId,
			Authentication principal, ServerWebExchange exchange) {
		
		log.debug("Loading authorized client for clientRegistrationId " + clientRegistrationId
				+ ", principal " + principal + ", and exchange " + exchange);
		
		return LecrUtils.fetchCookie(exchange, LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME)
				.map(this::deserialize)
				.orElse(Mono.empty());
	}

	@Override
	public Mono<Void> saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal,
			ServerWebExchange exchange) {
		
		log.debug("Saving authorized client " + authorizedClient
				+ " for principal " + principal + ", and exchange " + exchange);

		ServerHttpResponse response = exchange.getResponse();
		
		Assert.notNull(exchange, "exchange cannot be null");
		if (authorizedClient == null) {
			
			deleteCookies(exchange, LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME, LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
			return Mono.empty();
		}
		
		ResponseCookie cookie = ResponseCookie
				.from(LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME, LecUtils.serialize(authorizedClient))
				.path("/")
				.httpOnly(true)
				.maxAge(cookieExpirySecs)
				.build();

		response.addCookie(cookie);
		
		String lemonRedirectUri = exchange.getRequest()
				.getQueryParams().getFirst(LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
		
		if (StringUtils.isNotBlank(lemonRedirectUri)) {
			
			cookie = ResponseCookie
					.from(LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME, lemonRedirectUri)
					.path("/")
					.httpOnly(true)
					.maxAge(cookieExpirySecs)
					.build();

			response.addCookie(cookie);
		}
		
		return Mono.empty();
	}

	@Override
	public Mono<Void> removeAuthorizedClient(String clientRegistrationId, Authentication principal,
			ServerWebExchange exchange) {
		
		log.debug("Deleting authorized client for clientRegistrationId " + clientRegistrationId
				+ ", principal " + principal + ", and exchange " + exchange);

		deleteCookies(exchange, LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME);
		return Mono.empty();
	}
	
	public static void deleteCookies(ServerWebExchange exchange, String ...cookiesToDelete) {
		
		MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
		MultiValueMap<String, ResponseCookie> responseCookies = exchange.getResponse().getCookies();
		
		for (int i = 0; i < cookiesToDelete.length; i++)
			if (cookies.getFirst(cookiesToDelete[i]) != null) {
				
				ResponseCookie cookie = ResponseCookie.from(cookiesToDelete[i], "")
					.path("/")
					.maxAge(0L)
					.build();
				
				responseCookies.put(cookiesToDelete[i], Collections.singletonList(cookie));
			}
	}

	private Mono<OAuth2AuthorizedClient> deserialize(HttpCookie cookie) {		
		return Mono.just(LecUtils.deserialize(cookie.getValue()));
	}

}
