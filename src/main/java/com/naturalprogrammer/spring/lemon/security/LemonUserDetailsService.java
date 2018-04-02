package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
public class LemonUserDetailsService
	<U extends AbstractUser<U,ID>, ID extends Serializable>
implements UserDetailsService {

	private static final Log log = LogFactory.getLog(LemonUserDetailsService.class);

	private final AbstractUserRepository<U,ID> userRepository;
	
	public LemonUserDetailsService(AbstractUserRepository<U, ID> userRepository) {
		
		this.userRepository = userRepository;
		log.info("Created");
	}
	
	@Override
	public LemonPrincipal<ID> loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		log.debug("Loading user having username: " + username);
		
		// delegates to findUserByUsername
		U user = findUserByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException(username));

		log.debug("Loaded user having username: " + username);

		return new LemonPrincipal<>(user.toUserDto());
	}

	/**
	 * Finds a user by the given username. Override this
	 * if you aren't using email as the username.
	 * 
	 * @param username
	 * @return
	 */
	public Optional<U> findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}
}
