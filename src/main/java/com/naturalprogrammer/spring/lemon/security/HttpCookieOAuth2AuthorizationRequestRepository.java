package com.naturalprogrammer.spring.lemon.security;

import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.Assert;

public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
	
	private static final String COOKIE_NAME = "SpringLemonOAuth2AuthorizationRequest";

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		
		Assert.notNull(request, "request cannot be null");
		
		return fetchCookie(request)
				.map(this::toOAuth2AuthorizationRequest)
				.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
			HttpServletResponse response) {
		
		Assert.notNull(request, "request cannot be null");
		Assert.notNull(response, "response cannot be null");
		
		if (authorizationRequest == null) {
			
			deleteCookie(request, response);
			return;
		}
		
		Cookie cookie = new Cookie(COOKIE_NAME, fromAuthorizationRequest(authorizationRequest));
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	
	private String fromAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
		
		return Base64.getUrlEncoder().encodeToString(
				SerializationUtils.serialize(authorizationRequest));
	}

	private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
		
		fetchCookie(request).ifPresent(cookie -> {
			
			cookie.setValue("");
	        cookie.setMaxAge(0);
	        response.addCookie(cookie);
		});
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
		
		return loadAuthorizationRequest(request);
	}
	
	private Optional<Cookie> fetchCookie(HttpServletRequest request) {
		
		Cookie[] cookies = request.getCookies();

		if (cookies != null && cookies.length > 0)
			for (int i = 0; i < cookies.length; i++)
				if (cookies[i].getName().equals(COOKIE_NAME))
					return Optional.of(cookies[i]);
		
		return Optional.empty();
	}
	
	private OAuth2AuthorizationRequest toOAuth2AuthorizationRequest(Cookie cookie) {
		
		return SerializationUtils.deserialize(
				Base64.getUrlDecoder().decode(cookie.getValue()));
	}
}
