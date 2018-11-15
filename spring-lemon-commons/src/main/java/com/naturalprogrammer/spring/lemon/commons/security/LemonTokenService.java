package com.naturalprogrammer.spring.lemon.commons.security;

import java.util.Map;

import com.nimbusds.jwt.JWTClaimsSet;

public interface LemonTokenService {

	String LEMON_IAT = "lemon-iat";

	String createToken(String aud, String subject, Long expirationMillis, Map<String, Object> claimMap);
	String createToken(String audience, String subject, Long expirationMillis);
	JWTClaimsSet parseToken(String token, String audience);
	JWTClaimsSet parseToken(String token, String audience, long issuedAfter);
	<T> T parseClaim(String token, String claim);
}