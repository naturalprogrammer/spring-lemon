package com.naturalprogrammer.spring.lemon.commonsweb.security;

import com.naturalprogrammer.spring.lemon.commons.domain.AbstractAuditorAware;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Needed for auto-filling of the
 * AbstractAuditable columns of AbstractUser
 *  
 * @author Sanjay Patel
 */
public class LemonWebAuditorAware<ID extends Serializable>
extends AbstractAuditorAware<ID> {
	
    private static final Log log = LogFactory.getLog(LemonWebAuditorAware.class);
    
	public LemonWebAuditorAware() {		
		log.info("Created");
	}

	@Override
	protected UserDto currentUser() {		
		return LecwUtils.currentUser();
	}	
}
