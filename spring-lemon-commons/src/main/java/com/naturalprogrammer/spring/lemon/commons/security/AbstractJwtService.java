package com.naturalprogrammer.spring.lemon.commons.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Common JWT Service
 */
public abstract class AbstractJwtService implements LemonTokenService {
	
	private static final Log log = LogFactory.getLog(AbstractJwtService.class);
	
	protected Payload createPayload(String aud, String subject, Long expirationMillis, Map<String, Object> claimMap) {
		
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
		
		builder
    		//.issueTime(new Date())
    		.expirationTime(new Date(System.currentTimeMillis() + expirationMillis))
    		.audience(aud)
    		.subject(subject)
    		.claim(LEMON_IAT, System.currentTimeMillis());
		
		claimMap.forEach(builder::claim);
		
		JWTClaimsSet claims = builder.build();

    	return new Payload(claims.toJSONObject());
	}

	
	@Override
	public String createToken(String audience, String subject, Long expirationMillis) {

		return createToken(audience, subject, expirationMillis, new HashMap<>());
	}

	
	@Override
	public JWTClaimsSet parseToken(String token, String audience) {

		JWTClaimsSet claims = parseToken(token);
		LecUtils.ensureCredentials(audience != null &&
				claims.getAudience().contains(audience),
					"com.naturalprogrammer.spring.wrong.audience");
		
		long expirationTime = claims.getExpirationTime().getTime();
		long currentTime = System.currentTimeMillis();
		
		log.debug("Parsing JWT. Expiration time = " + expirationTime
				+ ". Current time = " + currentTime);
		
		LecUtils.ensureCredentials(expirationTime >= currentTime,
				"com.naturalprogrammer.spring.expiredToken");
		
		return claims;
	}


	@Override
	public JWTClaimsSet parseToken(String token, String audience, long issuedAfter) {
		
		JWTClaimsSet claims = parseToken(token, audience);
		
		long issueTime = (long) claims.getClaim(LEMON_IAT);
		LecUtils.ensureCredentials(issueTime >= issuedAfter,
				"com.naturalprogrammer.spring.obsoleteToken");

		return claims;
	}


	@Override
	public <T> T parseClaim(String token, String claim) {
		
		JWTClaimsSet claims = parseToken(token);
		return (T) claims.getClaim(claim);
	}
	

	protected abstract JWTClaimsSet parseToken(String token);	
}
