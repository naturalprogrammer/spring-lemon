package com.naturalprogrammer.spring.boot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.boot.SaUser;
import com.naturalprogrammer.spring.boot.SaUserRepository;

@Service
class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
	private SaUserRepository<? extends SaUser> userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {
		
		SaUser saUser = userRepository.findByEmail(email);
		if (saUser == null)
			throw new UsernameNotFoundException(email);

		return new UserDetailsImpl(saUser);

	}

}
