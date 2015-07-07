package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;

@Service
@ConditionalOnProperty(name="lemon.userdetailsservice.enabled", matchIfMissing=true)
class UserDetailsServiceImpl
	<U extends AbstractUser<U,ID>, ID extends Serializable>
implements UserDetailsService {

    @Autowired
	private AbstractUserRepository<U,ID> userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {
		
		U user = userRepository.findByEmail(email);
		if (user == null)
			throw new UsernameNotFoundException(email);

		return user;

	}

}
