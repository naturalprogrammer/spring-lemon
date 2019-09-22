package com.naturalprogrammer.spring.lemon.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.Assert;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;

/**
 * Cookie based repository for storing Authorization requests
 */
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
	
	private int cookieExpirySecs;
	
	public HttpCookieOAuth2AuthorizationRequestRepository(LemonProperties properties) {

		cookieExpirySecs = properties.getJwt().getShortLivedMillis() / 1000;
	}

	/**
	 * Load authorization request from cookie
	 */
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		
		Assert.notNull(request, "request cannot be null");
		
		return LecwUtils.fetchCookie(request, LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME)
					.map(this::deserialize)
					.orElse(null);
	}

	/**
	 * Save authorization request in cookie
	 */
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
			HttpServletResponse response) {
		
		Assert.notNull(request, "request cannot be null");
		Assert.notNull(response, "response cannot be null");
		
		if (authorizationRequest == null) {
			
			deleteCookies(request, response, LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME, LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
			return;
		}
		
		Cookie cookie = new Cookie(LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME, LecUtils.serialize(authorizationRequest));
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(cookieExpirySecs);
		response.addCookie(cookie);
		
		String lemonRedirectUri = request.getParameter(LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
		if (StringUtils.isNotBlank(lemonRedirectUri)) {

			cookie = new Cookie(LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME, lemonRedirectUri);
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			cookie.setMaxAge(cookieExpirySecs);
			response.addCookie(cookie);
		}		
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
		
		OAuth2AuthorizationRequest originalRequest = loadAuthorizationRequest(request);
		deleteCookies(request, response, LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME);
		return originalRequest;
	}
	
	/**
	 * Utility for deleting related cookies
	 */
	public static void deleteCookies(HttpServletRequest request, HttpServletResponse response, String... cookiesToDelete) {
		
		Cookie[] cookies = request.getCookies();

		if (cookies != null && cookies.length > 0)
			for (int i = 0; i < cookies.length; i++)
				if (ArrayUtils.contains(cookiesToDelete, cookies[i].getName())) {
					
					cookies[i].setValue("");
					cookies[i].setPath("/");
					cookies[i].setMaxAge(0);
					response.addCookie(cookies[i]);
				}
	}

	private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
		
		return LecUtils.deserialize(cookie.getValue());
	}

	@Deprecated
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
		throw new UnsupportedOperationException("Spring Security shouldn't have called the deprecated removeAuthorizationRequest(request)");
	}
}
