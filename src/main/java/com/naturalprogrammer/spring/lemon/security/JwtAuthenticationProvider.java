package com.naturalprogrammer.spring.lemon.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);

	private final JwtService jwtService;
	private UserDetailsService userDetailsService;
	
	public JwtAuthenticationProvider(JwtService jwtHelper, UserDetailsService userDetailsService) {

		this.jwtService = jwtHelper;
		this.userDetailsService = userDetailsService;
		
		log.debug("Created");
	}

	@Override
	public Authentication authenticate(Authentication auth) {
		
		log.debug("Authenticating ...");

		String jwtToken = (String) auth.getCredentials();
		
        String email = jwtService.parseAuthSubject(jwtToken);
        UserDetails user = userDetailsService.loadUserByUsername(email);

        log.debug("User found ...");

        return new JwtAuthenticationToken(user, jwtToken, user.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
