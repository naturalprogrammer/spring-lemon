package com.naturalprogrammer.spring.lemonreactive.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import com.github.fge.jsonpatch.JsonPatchException;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractDocument;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.nimbusds.jwt.JWTClaimsSet;

import reactor.core.publisher.Mono;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LerUtils {
	
	private static final Log log = LogFactory.getLog(LerUtils.class);
	
	private static Mono<Object> NOT_FOUND_MONO;
	
	@PostConstruct
	public void postConstruct() {
		NOT_FOUND_MONO = Mono.error(LexUtils.NOT_FOUND_EXCEPTION);
	}
	
	/**
	 * Gets the current-user
	 */
	public static <ID extends Serializable> Mono<Optional<UserDto>> currentUser() {
		
		return ReactiveSecurityContextHolder.getContext()
			.map(LecUtils::currentUser)
			.map(user -> Optional.of(user))
			.defaultIfEmpty(Optional.empty());
	}	

	
	/**
	 * Throws BadCredentialsException if 
	 * user's credentials were updated after the JWT was issued
	 */
	public static <U extends AbstractMongoUser<ID>, ID extends Serializable>
	void ensureCredentialsUpToDate(JWTClaimsSet claims, U user) {
		
		long issueTime = (long) claims.getClaim(JwtService.LEMON_IAT);

		log.debug("Ensuring credentials up to date. Issue time = "
				+ issueTime + ". User's credentials updated at" + user.getCredentialsUpdatedMillis());
		
		LecUtils.ensureCredentials(issueTime >= user.getCredentialsUpdatedMillis(),
				"com.naturalprogrammer.spring.obsoleteToken");
	}
	
	
	public static <T> Mono<T> notFoundMono() {
		return (Mono<T>) NOT_FOUND_MONO;
	}


	/**
	 * Throws a VersionException if the versions of the
	 * given entities aren't same.
	 * 
	 * @param original
	 * @param updated
	 */
	public static <ID extends Serializable>
	void ensureCorrectVersion(AbstractDocument<ID> original, AbstractDocument<ID> updated) {
		
		if (original.getVersion() != updated.getVersion())
			throw new VersionException(original.getClass().getSimpleName(), original.getId().toString());
	}

	public static<T> T applyPatch(T originalObj, String patchString) {

		try {
			return LecUtils.applyPatch(originalObj, patchString);
		} catch (IOException | JsonPatchException e) {
			throw new RuntimeException(e);
		}
	}
}
