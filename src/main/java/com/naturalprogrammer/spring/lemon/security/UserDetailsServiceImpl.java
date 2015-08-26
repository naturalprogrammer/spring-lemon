package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;

@Service
@ConditionalOnProperty(name="lemon.enabled.user-details-service", matchIfMissing=true)
public class UserDetailsServiceImpl
	<U extends AbstractUser<U,ID>, ID extends Serializable>
implements UserDetailsService {

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	protected AbstractUserRepository<U,ID> userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		log.debug("Loading user having username: " + username);
		
		Optional<U> optional = findUserByUsername(username);
		
		U user = optional.orElseThrow(() -> new UsernameNotFoundException(username));

		log.debug("Loaded user having username: " + username);
		
		return user.decorate(user);
	}

	protected Optional<U> findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}
}
