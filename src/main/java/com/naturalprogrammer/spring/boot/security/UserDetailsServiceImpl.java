package com.naturalprogrammer.spring.boot.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.boot.SaUser;
import com.naturalprogrammer.spring.boot.SaUserRepository;

@Service
class UserDetailsServiceImpl<ID extends Serializable> implements UserDetailsService {

    @Autowired
	private SaUserRepository<? extends SaUser<ID>, ? extends Serializable> userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {
		
		SaUser<ID> saUser = userRepository.findByEmail(email);
		if (saUser == null)
			throw new UsernameNotFoundException(email);

		return new UserDetailsImpl<ID>(saUser);

	}

}
