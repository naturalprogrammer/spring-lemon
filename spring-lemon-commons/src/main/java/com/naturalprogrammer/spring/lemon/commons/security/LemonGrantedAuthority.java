package com.naturalprogrammer.spring.lemon.commons.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

/**
 * Our implementation of GrantedAuthority.
 * Simpler than Spring Security's SimpleGrantedAuthority
 * and easily serializable.
 */
@Getter @Setter @AllArgsConstructor
public class LemonGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = -1447095521180508501L;
	
    private String authority;
}
