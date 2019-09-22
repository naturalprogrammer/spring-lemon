package com.naturalprogrammer.spring.lemon.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;

import lombok.AllArgsConstructor;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 * 
 * @author Sanjay Patel
 */
@AllArgsConstructor
public class OAuth2AuthenticationSuccessHandler
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(OAuth2AuthenticationSuccessHandler.class);

	private LemonProperties properties;
	private BlueTokenService blueTokenService;

	@Override
	protected String determineTargetUrl(HttpServletRequest request,
			HttpServletResponse response) {
		
		UserDto currentUser = LecwUtils.currentUser();
		
		String shortLivedAuthToken = blueTokenService.createToken(
				BlueTokenService.AUTH_AUDIENCE,
				currentUser.getUsername(),
				(long) properties.getJwt().getShortLivedMillis());

		String targetUrl = LecwUtils.fetchCookie(request,
				LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME)
				.map(Cookie::getValue)
				.orElse(properties.getOauth2AuthenticationSuccessUrl());
		
		HttpCookieOAuth2AuthorizationRequestRepository.deleteCookies(request, response,
				LecUtils.AUTHORIZATION_REQUEST_COOKIE_NAME,
				LecUtils.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME);
		
		return targetUrl + shortLivedAuthToken;
	}
}
