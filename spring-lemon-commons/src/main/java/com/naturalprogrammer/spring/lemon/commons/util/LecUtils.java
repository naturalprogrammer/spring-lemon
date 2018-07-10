package com.naturalprogrammer.spring.lemon.commons.util;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LecUtils {
	
	private static final Log log = LogFactory.getLog(LecUtils.class);
	
	// Computed authorities
	public static final String GOOD_ADMIN = "GOOD_ADMIN";
	public static final String GOOD_USER = "GOOD_USER";
	
	
	/**
	 * Extracts the current-user from authentication object
	 * 
	 * @param auth
	 * @return
	 */
	public static <ID extends Serializable> UserDto<ID> currentUser(Authentication auth) {
		
	    if (auth != null) {
	      Object principal = auth.getPrincipal();
	      if (principal instanceof LemonPrincipal<?>) {
	        return ((LemonPrincipal<ID>) principal).currentUser();
	      }
	    }
	    return null;	  
	}
}
