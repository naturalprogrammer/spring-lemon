package com.naturalprogrammer.spring.lemon.commonsreactive.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naturalprogrammer.spring.lemon.commons.domain.AbstractAuditorAware;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;

/**
 * Needed for auto-filling of the
 * AbstractAuditable columns of AbstractUser
 *  
 * @author Sanjay Patel
 */
public class LemonReactiveAuditorAware<ID extends Serializable>
extends AbstractAuditorAware<ID> {
	
    private static final Log log = LogFactory.getLog(LemonReactiveAuditorAware.class);
    
	public LemonReactiveAuditorAware() {		
		log.info("Created");
	}

	@Override
	protected UserDto currentUser() {
		
		// TODO: Can't return a mono, as below
		// So, sorry, no reactive auditing until Spring Data supports it
		// return LecrUtils.currentUser();
		
		return null;
	}	
}
