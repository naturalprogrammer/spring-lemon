package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class LemonTokenAuthenticationFilter	extends GenericFilterBean {
	
    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);
	
	private AuthenticationManager authenticationManager;
	
	public LemonTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
		
		this.authenticationManager = authenticationManager;
		log.info("Created");
	}

	public static boolean tokenPresent(HttpServletRequest request) {
		
		String header = request.getHeader(LemonSecurityConfig.TOKEN_REQUEST_HEADER);		
		return header != null && header.startsWith(LemonSecurityConfig.TOKEN_PREFIX);
	}	

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		log.debug("Inside LemonTokenAuthenticationFilter ...");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
    	if (tokenPresent(req)) {
			
			log.debug("Found a token");
			
		    String token = req.getHeader(LemonSecurityConfig.TOKEN_REQUEST_HEADER).substring(7);
		    JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token);
		    
		    try {
		    	
		    	Authentication auth = authenticationManager.authenticate(authRequest);
		    	SecurityContextHolder.getContext().setAuthentication(auth);
		    	
				log.debug("Token authentication successful");
				    		    	
		    } catch (Exception e) {
		    	
				log.debug("Token authentication failed - " + e.getMessage());
				
		    	res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
						"Authentication Failed: " + e.getMessage());
		    	
		    	return;
		    }
		    
		} else
		
			log.debug("Token authentication skipped");
		
		chain.doFilter(request, response);
	}
}
