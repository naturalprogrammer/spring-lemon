package com.naturalprogrammer.spring.lemon.util;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.LemonTokenService;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LemonUtils {
	
	private static final Log log = LogFactory.getLog(LemonUtils.class);

	public LemonUtils() {
		
		log.info("Created");
	}


	/**
	 * Signs a user in
	 * 
	 * @param user
	 */
	public static <U extends AbstractUser<ID>, ID extends Serializable>
	void login(U user) {
		
		LemonPrincipal principal = new LemonPrincipal(user.toUserDto());

		Authentication authentication = // make the authentication object
	    	new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

	    SecurityContextHolder.getContext().setAuthentication(authentication); // put that in the security context
	    principal.eraseCredentials();
	}
	
	
	/**
	 * Throws BadCredentialsException if 
	 * user's credentials were updated after the JWT was issued
	 */
	public static <U extends AbstractUser<ID>, ID extends Serializable>
	void ensureCredentialsUpToDate(JWTClaimsSet claims, U user) {
		
		long issueTime = (long) claims.getClaim(LemonTokenService.LEMON_IAT);

		LecUtils.ensureCredentials(issueTime >= user.getCredentialsUpdatedMillis(),
				"com.naturalprogrammer.spring.obsoleteToken");
	}
}
