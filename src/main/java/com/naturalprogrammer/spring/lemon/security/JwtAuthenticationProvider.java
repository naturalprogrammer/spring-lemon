package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.nimbusds.jwt.JWTClaimsSet;

public class JwtAuthenticationProvider
<U extends AbstractUser<U,ID>, ID extends Serializable> implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);

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
        LemonPrincipal<ID> principal = new LemonPrincipal<ID>(user.toSpringUser());
        		
        return new JwtAuthenticationToken(principal, token, principal.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
