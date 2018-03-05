package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * OAuth2 Authentication failure handler for removing the 
 * 
 * @author Sanjay Patel
 */
public class OAuth2AuthenticationFailureHandler
	extends SimpleUrlAuthenticationFailureHandler {
	
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		
		HttpCookieOAuth2AuthorizationRequestRepository.deleteCookies(request, response);
		super.onAuthenticationFailure(request, response, exception);
	}
}
