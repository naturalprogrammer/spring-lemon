package com.naturalprogrammer.spring.boot.security;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.naturalprogrammer.spring.boot.SaUser;
import com.naturalprogrammer.spring.boot.SaUser.Role;

public class UserDetailsImpl implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3862469610636495180L;

	private SaUser saUser;
	
	public SaUser getUser() {
		return saUser;
	}

	public void setUser(SaUser saUser) {
		this.saUser = saUser;
	}

	public UserDetailsImpl(SaUser saUser) {
		this.saUser = saUser;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(
				saUser.getRoles().size() + 1);

		for (Role role : saUser.getRoles())
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		return authorities;
		
	}

	@Override
	public String getPassword() {
		return saUser.getPassword();
	}

	@Override
	public String getUsername() {
		return saUser.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
