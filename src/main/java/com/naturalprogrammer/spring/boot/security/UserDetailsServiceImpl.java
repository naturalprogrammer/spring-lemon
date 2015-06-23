package com.naturalprogrammer.spring.boot.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.boot.domain.BaseUser;
import com.naturalprogrammer.spring.boot.domain.BaseUserRepository;

@Service
class UserDetailsServiceImpl<U extends BaseUser<U,ID>, ID extends Serializable> implements UserDetailsService {

    @Autowired
	private BaseUserRepository<U,ID> userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {
		
		U user = userRepository.findByEmail(email);
		if (user == null)
			throw new UsernameNotFoundException(email);

		return user;
		//return new UserDetailsImpl<U,ID>(user);

	}

}
