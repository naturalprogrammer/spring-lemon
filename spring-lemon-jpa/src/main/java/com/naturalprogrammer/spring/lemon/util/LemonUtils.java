package com.naturalprogrammer.spring.lemon.util;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsjpa.LemonEntity;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
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
	 * Throws a VersionException if the versions of the
	 * given entities aren't same.
	 * 
	 * @param original
	 * @param updated
	 */
	public static <U extends AbstractUser<ID>, ID extends Serializable>
	void ensureCorrectVersion(LemonEntity<ID> original, LemonEntity<ID> updated) {
		
		if (original.getVersion() != updated.getVersion())
			throw new VersionException(original.getClass().getSimpleName(), original.getId().toString());
	}
	
	
	/**
	 * Throws BadCredentialsException if 
	 * user's credentials were updated after the JWT was issued
	 */
	public static <U extends AbstractUser<ID>, ID extends Serializable>
	void ensureCredentialsUpToDate(JWTClaimsSet claims, U user) {
		
		long issueTime = (long) claims.getClaim(JwtService.LEMON_IAT);

		LecUtils.ensureCredentials(issueTime >= user.getCredentialsUpdatedMillis(),
				"com.naturalprogrammer.spring.obsoleteToken");
	}
}
