/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.spring.lemon.commons.security;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security Principal, implementing both OidcUser, UserDetails
 */
@Getter @Setter @RequiredArgsConstructor
public class LemonPrincipal implements OidcUser, UserDetails, CredentialsContainer {

	private static final long serialVersionUID = -7849730155307434535L;
	
	@Getter(AccessLevel.NONE)
	private final UserDto userDto;
	
	private Map<String, Object> attributes;
	private String name;
	private Map<String, Object> claims;
	private OidcUserInfo userInfo;
	private OidcIdToken idToken;
	
	public UserDto currentUser() {
		return userDto;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		Set<String> roles = userDto.getRoles();
		
		Collection<LemonGrantedAuthority> authorities = roles.stream()
				.map(role -> new LemonGrantedAuthority("ROLE_" + role))
				.collect(Collectors.toCollection(() ->
					new ArrayList<LemonGrantedAuthority>(roles.size() + 2))); 
		
		if (userDto.isGoodUser()) {
			
			authorities.add(new LemonGrantedAuthority("ROLE_"
					+ LecUtils.GOOD_USER));
			
			if (userDto.isGoodAdmin())
				authorities.add(new LemonGrantedAuthority("ROLE_"
					+ LecUtils.GOOD_ADMIN));
		}
		
		return authorities;	
	}

	// UserDetails ...

	@Override
	public String getPassword() {

		return userDto.getPassword();
	}

	@Override
	public String getUsername() {

		return userDto.getUsername();
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

	@Override
	public void eraseCredentials() {
		
		userDto.setPassword(null);
		attributes = null;
		claims = null;
		userInfo = null;
		idToken = null;
	}
}
