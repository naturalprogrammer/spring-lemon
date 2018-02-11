package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationProvider
<U extends AbstractUser<U,ID>, ID extends Serializable> implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);

	private final JwtService jwtService;
	private LemonUserDetailsService<U, ID> userDetailsService;
	
	public JwtAuthenticationProvider(JwtService jwtHelper, LemonUserDetailsService<U, ID> userDetailsService) {

		this.jwtService = jwtHelper;
		this.userDetailsService = userDetailsService;
		
		log.debug("Created");
	}

	@Override
	public Authentication authenticate(Authentication auth) {
		
		log.debug("Authenticating ...");

		String jwtToken = (String) auth.getCredentials();
		
        String username = jwtService.parseAuthSubject(jwtToken);
        U user = userDetailsService.findUserByUsername(username)
        		.orElseThrow(() -> new UsernameNotFoundException(username));

        log.debug("User found ...");
        
        if (jwtService.parseIssuedAt(jwtToken).before(user.getCredentialsUpdatedAt()))
        	throw new JwtException(LemonUtils.getMessage("credentialsChanged"));

        LemonPrincipal<ID> principal = new LemonPrincipal<ID>(user.toSpringUser());
        		
        return new JwtAuthenticationToken(principal, jwtToken, principal.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
