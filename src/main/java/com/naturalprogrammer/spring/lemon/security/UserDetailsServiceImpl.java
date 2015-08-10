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
@ConditionalOnProperty(name="lemon.enabled.userdetailsservice", matchIfMissing=true)
class UserDetailsServiceImpl
	<U extends AbstractUser<U,ID>, ID extends Serializable>
implements UserDetailsService {

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private AbstractUserRepository<U,ID> userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {
		
		log.debug("Loading user having email: " + email);
		
		Optional<U> optional = userRepository.findByEmail(email);
		
		U user = optional.orElseThrow(() -> new UsernameNotFoundException(email));

		log.debug("Loaded user having email: " + email);
		
		return user.decorate(user);

	}

}
