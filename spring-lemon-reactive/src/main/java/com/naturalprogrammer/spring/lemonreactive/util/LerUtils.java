package com.naturalprogrammer.spring.lemonreactive.util;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;

import reactor.core.publisher.Mono;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LerUtils {
	
	private static final Log log = LogFactory.getLog(LerUtils.class);
	
	/**
	 * Gets the current-user
	 */
	public static <ID extends Serializable> Mono<UserDto<ID>>  currentUser() {
		
		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(LecUtils::currentUser);
	}
}
