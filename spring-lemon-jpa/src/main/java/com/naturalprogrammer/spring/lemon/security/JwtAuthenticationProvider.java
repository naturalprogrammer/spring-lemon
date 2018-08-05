package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.naturalprogrammer.spring.lemon.commons.security.JwtAuthenticationToken;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Authentication provider for JWT token authentication
 */
public class JwtAuthenticationProvider
<U extends AbstractUser<U,ID>, ID extends Serializable> implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog(JwtAuthenticationProvider.class);

	private final JwtService jwtService;
	private LemonUserDetailsService<U, ID> userDetailsService;
	
	public JwtAuthenticationProvider(JwtService jwtService, LemonUserDetailsService<U, ID> userDetailsService) {

		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		
		log.debug("Created");
	}

	@Override
	public Authentication authenticate(Authentication auth) {
		
		log.debug("Authenticating ...");

		String token = (String) auth.getCredentials();
		
		JWTClaimsSet claims = jwtService.parseToken(token, JwtService.AUTH_AUDIENCE);
		
        String username = claims.getSubject();
        U user = userDetailsService.findUserByUsername(username)
        		.orElseThrow(() -> new UsernameNotFoundException(username));

        log.debug("User found ...");

        LemonUtils.ensureCredentialsUpToDate(claims, user);
        LemonPrincipal principal = new LemonPrincipal(user.toUserDto());
        		
        return new JwtAuthenticationToken(principal, token, principal.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
