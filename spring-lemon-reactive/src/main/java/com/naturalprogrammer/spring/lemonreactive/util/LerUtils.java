package com.naturalprogrammer.spring.lemonreactive.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;

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
	public static Mono<User> currentUser() {
		
		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(LerUtils::currentUser);
	}
	

	/**
	 * Extracts the current-user from authentication object
	 * 
	 * @param auth
	 * @return
	 */
	public static User currentUser(Authentication auth) {
		
	    if (auth != null) {
	      Object principal = auth.getPrincipal();
	      if (principal instanceof User) {
	        return (User) principal;
	      }
	    }
	    return null;	  
	}	

}
