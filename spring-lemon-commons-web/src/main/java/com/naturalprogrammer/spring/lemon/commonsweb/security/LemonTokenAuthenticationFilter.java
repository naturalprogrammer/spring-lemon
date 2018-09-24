package com.naturalprogrammer.spring.lemon.commonsweb.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.naturalprogrammer.spring.lemon.commons.security.JwtAuthenticationToken;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;

/**
 * Filter for token authentication
 */
public class LemonTokenAuthenticationFilter	extends OncePerRequestFilter {
	
    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);
	
	private AuthenticationManager authenticationManager;
	
	public LemonTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
		
		this.authenticationManager = authenticationManager;
		log.info("Created");
	}

	/**
	 * Checks if a "Bearer " token is present
	 */
	protected boolean tokenPresent(HttpServletRequest request) {
		
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);		
		return header != null && header.startsWith(LecUtils.TOKEN_PREFIX);
	}	

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.debug("Inside LemonTokenAuthenticationFilter ...");
		
    	if (tokenPresent(request)) {
			
			log.debug("Found a token");
			
		    String token = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
		    JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token);
		    
		    try {
		    	
		    	Authentication auth = authenticationManager.authenticate(authRequest);
		    	SecurityContextHolder.getContext().setAuthentication(auth);
		    	
				log.debug("Token authentication successful");
				    		    	
		    } catch (Exception e) {
		    	
				log.debug("Token authentication failed - " + e.getMessage());
				
		    	response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
						"Authentication Failed: " + e.getMessage());
		    	
		    	return;
		    }
		    
		} else
		
			log.debug("Token authentication skipped");
		
		filterChain.doFilter(request, response);
	}
}
