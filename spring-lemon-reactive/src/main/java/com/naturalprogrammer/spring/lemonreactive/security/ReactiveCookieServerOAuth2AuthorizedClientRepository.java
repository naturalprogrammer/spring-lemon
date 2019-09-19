package com.naturalprogrammer.spring.lemonreactive.security;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;

import reactor.core.publisher.Mono;

public class ReactiveCookieServerOAuth2AuthorizedClientRepository implements ServerOAuth2AuthorizedClientRepository {

	public static final String AUTHORIZATION_REQUEST_COOKIE_NAME = "lemon_oauth2_authorization_request";
	public static final String LEMON_REDIRECT_URI_COOKIE_PARAM_NAME = "lemon_redirect_uri";

	private int cookieExpirySecs;
	
	public ReactiveCookieServerOAuth2AuthorizedClientRepository(LemonProperties properties) {

		cookieExpirySecs = properties.getJwt().getShortLivedMillis() / 1000;
	}

	@Override
	public Mono<OAuth2AuthorizedClient> loadAuthorizedClient(String clientRegistrationId,
			Authentication principal, ServerWebExchange exchange) {
		
		return LerUtils.fetchCookie(exchange, AUTHORIZATION_REQUEST_COOKIE_NAME)
				.map(this::deserialize)
				.orElse(Mono.empty());
	}

	@Override
	public Mono<Void> saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal,
			ServerWebExchange exchange) {
		
		ServerHttpResponse response = exchange.getResponse();
		
		Assert.notNull(exchange, "exchange cannot be null");
		if (authorizedClient == null) {
			
			deleteCookies(exchange, AUTHORIZATION_REQUEST_COOKIE_NAME, LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
			return Mono.empty();
		}
		
		ResponseCookie cookie = ResponseCookie
				.from(AUTHORIZATION_REQUEST_COOKIE_NAME, LecUtils.serialize(authorizedClient))
				.path("/")
				.httpOnly(true)
				.maxAge(cookieExpirySecs)
				.build();

		response.addCookie(cookie);
		
		String lemonRedirectUri = exchange.getRequest()
				.getQueryParams().getFirst(LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
		
		if (StringUtils.isNotBlank(lemonRedirectUri)) {
			
			cookie = ResponseCookie
					.from(LEMON_REDIRECT_URI_COOKIE_PARAM_NAME, lemonRedirectUri)
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
		
		deleteCookies(exchange, AUTHORIZATION_REQUEST_COOKIE_NAME);
		return Mono.empty();
	}
	
	private void deleteCookies(ServerWebExchange exchange, String ...cookiesToDelete) {
		
		MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
		MultiValueMap<String, ResponseCookie> responseCookies = exchange.getResponse().getCookies();
		
		for (int i = 0; i < cookiesToDelete.length; i++)
			if (cookies.getFirst(cookiesToDelete[i]) != null) {
				
				ResponseCookie cookie = ResponseCookie.from(cookiesToDelete[i], "")
					.path("/")
					.maxAge(0L)
					.build();
				
				responseCookies.put(cookiesToDelete[i], Collections.singletonList(cookie));
			};			
	}

	private Mono<OAuth2AuthorizedClient> deserialize(HttpCookie cookie) {		
		return Mono.just(LecUtils.deserialize(cookie.getValue()));
	}

}
