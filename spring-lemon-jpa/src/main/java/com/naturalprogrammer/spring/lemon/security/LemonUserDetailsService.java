package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;

/**
 * UserDetailsService, as required by Spring Security.
 * 
 * @author Sanjay Patel
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
			.orElseThrow(() -> new UsernameNotFoundException(
				LexUtils.getMessage("com.naturalprogrammer.spring.userNotFound", username)));

		log.debug("Loaded user having username: " + username);

		return new LemonPrincipal<>(user.toUserDto());
	}

	/**
	 * Finds a user by the given username. Override this
	 * if you aren't using email as the username.
	 */
	public Optional<U> findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}
}
