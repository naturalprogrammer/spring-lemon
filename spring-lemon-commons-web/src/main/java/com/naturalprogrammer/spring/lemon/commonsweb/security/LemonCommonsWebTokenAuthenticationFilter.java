package com.naturalprogrammer.spring.lemon.commonsweb.security;

import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for token authentication
 */
@AllArgsConstructor
public class LemonCommonsWebTokenAuthenticationFilter extends OncePerRequestFilter {
	
    private static final Log log = LogFactory.getLog(LemonCommonsWebTokenAuthenticationFilter.class);
    
    private BlueTokenService blueTokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.debug("Inside LemonTokenAuthenticationFilter ...");
		
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);				
		
    	if (header != null && header.startsWith(LecUtils.TOKEN_PREFIX)) { // token present
			
			log.debug("Found a token");			
		    String token = header.substring(7);
		    
		    try {
		    	
		    	Authentication auth = createAuthToken(token);
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

	protected Authentication createAuthToken(String token) {
		
		JWTClaimsSet claims = blueTokenService.parseToken(token, BlueTokenService.AUTH_AUDIENCE);
		UserDto userDto = LecUtils.getUserDto(claims);
		if (userDto == null)
			userDto = fetchUserDto(claims);
		
        LemonPrincipal principal = new LemonPrincipal(userDto);
        		
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
	}

	/**
	 * Default behaviour is to throw error. To be overridden in auth service.
	 * 
	 * @param username
	 * @return
	 */
	protected UserDto fetchUserDto(JWTClaimsSet claims) {
		throw new AuthenticationCredentialsNotFoundException(
				LexUtils.getMessage("com.naturalprogrammer.spring.userClaimAbsent"));
	}
}
