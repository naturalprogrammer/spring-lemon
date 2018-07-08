package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 * 
 * @author Sanjay Patel
 */
public class OAuth2AuthenticationSuccessHandler<ID extends Serializable>
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(OAuth2AuthenticationSuccessHandler.class);

	private LemonProperties properties;
	private JwtService jwtService;

	public OAuth2AuthenticationSuccessHandler(LemonProperties properties, JwtService jwtService) {

		this.properties = properties;
		this.jwtService = jwtService;

		log.info("Created");
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request,
			HttpServletResponse response) {
		
		UserDto<ID> currentUser = LemonUtils.currentUser();
		
		String shortLivedAuthToken = jwtService.createToken(
				JwtService.AUTH_AUDIENCE,
				currentUser.getUsername(),
				(long) properties.getJwt().getShortLivedMillis());

		String targetUrl = LemonUtils.fetchCookie(request,
				HttpCookieOAuth2AuthorizationRequestRepository.LEMON_REDIRECT_URI_COOKIE_PARAM_NAME)
				.map(Cookie::getValue)
				.orElse(properties.getOauth2AuthenticationSuccessUrl());
		
		HttpCookieOAuth2AuthorizationRequestRepository.deleteCookies(request, response);
		return targetUrl + shortLivedAuthToken;
	}
}
