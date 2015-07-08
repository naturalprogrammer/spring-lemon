package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

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
 * See https://spring.io/guides/tutorials/spring-security-and-angular-js/
 * 
 * @author skpat_000
 *
 */
public class CsrfCookieFilter extends OncePerRequestFilter {
	
	private final Log log = LogFactory.getLog(getClass());

	public static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
					throws ServletException, IOException {		

		log.debug("Inside CsrfCookieFilter ...");

		CsrfToken csrf = (CsrfToken)
			request.getAttribute(CsrfToken.class.getName()); // Or "_csrf" (See CSRFFilter.doFilterInternal).
		
		if (csrf != null) {
			Cookie cookie = WebUtils.getCookie(
				request, XSRF_TOKEN_COOKIE_NAME);
			String token = csrf.getToken();
			if (cookie==null ||
				token!=null && !token.equals(cookie.getValue())) {
				cookie = new Cookie(XSRF_TOKEN_COOKIE_NAME, token);
				cookie.setPath("/");
				// cookie.setHttpOnly(true); client interceptor can't see the cookie is this is set
				response.addCookie(cookie);
				
				log.debug("Added cookie " + XSRF_TOKEN_COOKIE_NAME + ": " + token);
			}
		}		
		filterChain.doFilter(request, response);
	}
}
