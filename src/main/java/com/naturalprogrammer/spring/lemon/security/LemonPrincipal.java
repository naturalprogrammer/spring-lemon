package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class LemonPrincipal<PK extends Serializable> implements OidcUser, UserDetails, CredentialsContainer {

	private static final long serialVersionUID = -7849730155307434535L;
	
	private PK userId;
	
	private Collection<? extends GrantedAuthority> authorities;
	private Map<String, Object> attributes;
	private String name;
	private Map<String, Object> claims;
	private OidcUserInfo userInfo;
	private OidcIdToken idToken;

	private String username;
	private String password;
	
	private Serializable tag;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return authorities;
	}

	@Override
	public Map<String, Object> getAttributes() {

		return attributes;
	}

	@Override
	public String getName() {

		return name;
	}

	@Override
	public Map<String, Object> getClaims() {

		return claims;
	}

	@Override
	public OidcUserInfo getUserInfo() {

		return userInfo;
	}

	@Override
	public OidcIdToken getIdToken() {

		return idToken;
	}

	// UserDetails ...

	@Override
	public String getPassword() {

		return password;
	}

	@Override
	public String getUsername() {

		return username;
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

	public PK getUserId() {
		return userId;
	}

	public void setUserId(PK userId) {
		this.userId = userId;
	}

	public Serializable getTag() {
		return tag;
	}

	public void setTag(Serializable tag) {
		this.tag = tag;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setClaims(Map<String, Object> claims) {
		this.claims = claims;
	}

	public void setUserInfo(OidcUserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public void setIdToken(OidcIdToken idToken) {
		this.idToken = idToken;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public void eraseCredentials() {
		
		password = null;
		attributes = null;
		claims = null;
		userInfo = null;
		idToken = null;
	}
}
