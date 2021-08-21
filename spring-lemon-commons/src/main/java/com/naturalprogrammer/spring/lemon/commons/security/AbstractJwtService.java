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
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
