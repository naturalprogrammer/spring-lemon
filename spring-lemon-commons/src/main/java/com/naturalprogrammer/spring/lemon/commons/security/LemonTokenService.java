package com.naturalprogrammer.spring.lemon.commons.security;

import java.util.Map;

import com.nimbusds.jwt.JWTClaimsSet;

public interface LemonTokenService {

	String LEMON_IAT = "lemon-iat";

	/**
	 * Creates a token
	 */
	String createToken(String aud, String subject, Long expirationMillis, Map<String, Object> claimMap);

	/**
	 * Creates a token
	 */
	String createToken(String audience, String subject, Long expirationMillis);

	/**
	 * Parses a token
	 */
	JWTClaimsSet parseToken(String token, String audience);

	/**
	 * Parses a token
	 */
	JWTClaimsSet parseToken(String token, String audience, long issuedAfter);

	/**
	 * Parses a claim
	 */
	<T> T parseClaim(String token, String claim);

}