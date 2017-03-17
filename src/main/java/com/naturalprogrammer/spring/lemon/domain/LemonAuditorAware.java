package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;

/**
 * Needed for auto-filling of the
 * AbstractAuditable columns of AbstractUser
 *  
 * @author Sanjay Patel
 *
 * @param <U>	The User class
 * @param <ID>	The Primary key type of User class 
 */
@Component
@ConditionalOnMissingBean(AuditorAware.class)
public class LemonAuditorAware
	<U extends AbstractUser<U,ID>,
	 ID extends Serializable>
implements AuditorAware<U> {
	
    private static final Log log = LogFactory.getLog(LemonAuditorAware.class);
    
	public LemonAuditorAware() {
		log.info("Created");
	}

	@Override
	public U getCurrentAuditor() {
		return LemonUtil.getUser();
	}	
}
