package com.naturalprogrammer.spring.boot.security;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.naturalprogrammer.spring.boot.user.BaseUser;
import com.naturalprogrammer.spring.boot.user.BaseUser.Role;

public class UserDetailsImpl implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3862469610636495180L;

	private BaseUser baseUser;
	
	public BaseUser getUser() {
		return baseUser;
	}

	public void setUser(BaseUser baseUser) {
		this.baseUser = baseUser;
	}

	public UserDetailsImpl(BaseUser baseUser) {
		this.baseUser = baseUser;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(
				baseUser.getRoles().size() + 1);

		for (Role role : baseUser.getRoles())
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		return authorities;
		
	}

	@Override
	public String getPassword() {
		return baseUser.getPassword();
	}

	@Override
	public String getUsername() {
		return baseUser.getEmail();
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
