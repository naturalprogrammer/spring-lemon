package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

/**
 * Filter for attaching the CSRF token as a cookie.
 * 
 * @see <a href="https://spring.io/guides/tutorials/spring-security-and-angular-js/#_csrf_protection">CSRF protection</a>
 * 
 * @author Sanjay Patel
 */
public class CsrfCookieFilter extends OncePerRequestFilter {
	
	private final Log log = LogFactory.getLog(getClass());

	// name of the cookie
	public static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";
	
	// name of the header to be receiving from the client
	public static final String XSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
					throws ServletException, IOException {		

		log.debug("Inside CsrfCookieFilter ...");

		// get the token
		CsrfToken csrf = (CsrfToken)
			request.getAttribute(CsrfToken.class.getName()); // Or "_csrf" (See CSRFFilter.doFilterInternal).
		
		if (csrf != null) { // if there is a token found
			Cookie cookie = WebUtils.getCookie(
				request, XSRF_TOKEN_COOKIE_NAME); // get the cookie
			
			String token = csrf.getToken();
			
			if (token != null &&					 // there is a token, AND
			    (cookie == null || 					 // the cookie is either not there
				!cookie.getValue().equals(token))) { // or not equal to the token
				
				// set the cookie
				cookie = new Cookie(XSRF_TOKEN_COOKIE_NAME, token);
				cookie.setPath("/");
				cookie.setHttpOnly(false); // client JavaScriot interceptor can't see the cookie if HttpOnly is true
				response.addCookie(cookie);
				
				log.debug("Added cookie " + XSRF_TOKEN_COOKIE_NAME + ": " + token);
			}
		}		
		filterChain.doFilter(request, response);
	}
}
