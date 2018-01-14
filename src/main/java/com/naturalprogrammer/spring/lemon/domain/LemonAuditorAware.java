package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.AuditorAware;

import com.naturalprogrammer.spring.lemon.security.SpringUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Needed for auto-filling of the
 * AbstractAuditable columns of AbstractUser
 *  
 * @author Sanjay Patel
 *
 * @param <U>	The User class
 * @param <ID>	The Primary key type of User class 
 */
public class LemonAuditorAware
	<U extends AbstractUser<U,ID>,
	 ID extends Serializable>
implements AuditorAware<U> {
	
    private static final Log log = LogFactory.getLog(LemonAuditorAware.class);
    
    private AbstractUserRepository<U,ID> userRepository;
    
	public LemonAuditorAware() {
		log.info("Created");
	}

	@Override
	public Optional<U> getCurrentAuditor() {
		
		SpringUser<ID> springUser = LemonUtils.getSpringUser();
		
		if (springUser == null)
			return Optional.empty();
		
		return userRepository.findById(springUser.getId());
	}	
}
