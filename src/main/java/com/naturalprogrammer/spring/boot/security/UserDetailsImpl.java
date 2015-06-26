//package com.naturalprogrammer.spring.boot.security;
//
//import java.io.Serializable;
//import java.util.Collection;
//import java.util.HashSet;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import com.naturalprogrammer.spring.boot.domain.AbstractUser;
//
//public class UserDetailsImpl<U extends AbstractUser<U,ID>, ID extends Serializable> implements UserDetails {
//
//
//	private static final long serialVersionUID = -3862469610636495180L;
//
//	private U user;
//	
//	public U getUser() {
//		return user;
//	}
//
//	public void setUser(U user) {
//		this.user = user;
//	}
//
//	public UserDetailsImpl(U user) {
//		this.user = user;
//	}
//
//	@Override
//	public Collection<? extends GrantedAuthority> getAuthorities() {
//		
//		Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(
//				user.getRoles().size() + 1);
//
//		for (String role : user.getRoles())
//			authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//
//		//authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//
//		return authorities;
//		
//	}
//
//	@Override
//	public String getPassword() {
//		return user.getPassword();
//	}
//
//	@Override
//	public String getUsername() {
//		return user.getEmail();
//	}
//
//	@Override
//	public boolean isAccountNonExpired() {
//		return true;
//	}
//
//	@Override
//	public boolean isAccountNonLocked() {
//		return true;
//	}
//
//	@Override
//	public boolean isCredentialsNonExpired() {
//		return true;
//	}
//
//	@Override
//	public boolean isEnabled() {
//		return true;
//	}
//
//}
