package com.naturalprogrammer.spring.lemon.commonsweb.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import com.naturalprogrammer.spring.lemon.commons.security.JwtAuthenticationToken;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Authentication provider for JWT token authentication
 */
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog(JwtAuthenticationProvider.class);

	private final JwtService jwtService;
	
	public JwtAuthenticationProvider(JwtService jwtService) {

		this.jwtService = jwtService;		
		log.debug("Created");
	}

	@Override
	public Authentication authenticate(Authentication auth) {
		
		log.debug("Authenticating ...");

		String token = (String) auth.getCredentials();
		
		JWTClaimsSet claims = jwtService.parseToken(token, JwtService.AUTH_AUDIENCE);
		UserDto userDto = LecUtils.getUserDto(claims);
		if (userDto == null)
			userDto = fetchUserDto(claims);
		
        LemonPrincipal principal = new LemonPrincipal(userDto);
        		
        return new JwtAuthenticationToken(principal, token, principal.getAuthorities());
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

	@Override
	public boolean supports(Class<?> authentication) {
		
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
