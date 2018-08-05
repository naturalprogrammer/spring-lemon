package com.naturalprogrammer.spring.lemon.commonsreactive.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import com.github.fge.jsonpatch.JsonPatchException;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

import reactor.core.publisher.Mono;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LecrUtils {
	
	private static final Log log = LogFactory.getLog(LecrUtils.class);
	
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
		
	public static <T> Mono<T> notFoundMono() {
		return (Mono<T>) NOT_FOUND_MONO;
	}

	public static<T> T applyPatch(T originalObj, String patchString) {

		try {
			return LecUtils.applyPatch(originalObj, patchString);
		} catch (IOException | JsonPatchException e) {
			throw new RuntimeException(e);
		}
	}
}
