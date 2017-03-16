package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;

/**
 * UserDetailsService, as required by Spring Security.
 * If this implementation does not meet your requirement,
 * just provide your own. Providing the property
 * lemon.enabled.user-details-service: false will
 * suppress this configuration. 
 * 
 * @author Sanjay Patel
 *
 * @param <U>	The user class
 * @param <ID>	Primary key class, e.g. Long
 */
@Service
@ConditionalOnProperty(name="lemon.enabled.user-details-service", matchIfMissing=true)
public class UserDetailsServiceImpl
	<U extends AbstractUser<U,ID>, ID extends Serializable>
implements UserDetailsService {

	private static final Log log = LogFactory.getLog(UserDetailsServiceImpl.class);

	protected AbstractUserRepository<U,ID> userRepository;
	
	public UserDetailsServiceImpl() {
		log.info("Created");
	}
	
	@Autowired	
	public void setUserRepository(AbstractUserRepository<U, ID> userRepository) {

		log.info("Setting userRepository");
		this.userRepository = userRepository;
	}

	@Override
	public U loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		log.debug("Loading user having username: " + username);
		
		// delegates to findUserByUsername
		U user = findUserByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException(username));

		user.decorate(user);
		
		log.debug("Loaded user having username: " + username);
		
		return user;
	}

	/**
	 * Finds a user by the given username. Override this
	 * if you aren't using email as the username.
	 * 
	 * @param username
	 * @return
	 */
	protected Optional<U> findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}
}
