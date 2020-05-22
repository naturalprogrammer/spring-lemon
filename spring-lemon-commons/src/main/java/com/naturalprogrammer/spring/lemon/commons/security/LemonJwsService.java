package com.naturalprogrammer.spring.lemon.commons.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.security.authentication.BadCredentialsException;

import java.text.ParseException;
import java.util.Map;

/**
 * JWS Service
 * 
 * Reference: https://connect2id.com/products/nimbus-jose-jwt/examples/jws-with-hmac
 */
public class LemonJwsService extends AbstractJwtService implements BlueTokenService {

	private JWSSigner signer;
	private JWSVerifier verifier;

	public LemonJwsService(String secret) throws JOSEException {
		
		signer = new MACSigner(secret);
		verifier = new MACVerifier(secret);
	}

	@Override
	public String createToken(String aud, String subject, Long expirationMillis, Map<String, Object> claimMap) {
		
		Payload payload = createPayload(aud, subject, expirationMillis, claimMap);

	   	// Prepare JWS object
		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), payload);

		try {
			// Apply the HMAC
			jwsObject.sign(signer);
			
		} catch (JOSEException e) {
			
			throw new RuntimeException(e);
		}

		// To serialize to compact form, produces something like
		// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
		return jwsObject.serialize();
	}

	/**
	 * Parses a token
	 */
	protected JWTClaimsSet parseToken(String token) {
		
		// Parse the JWS and verify it, e.g. on client-side
		JWSObject jwsObject;

		try {
			jwsObject = JWSObject.parse(token);
			if (jwsObject.verify(verifier))
				return JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
			
		} catch (JOSEException | ParseException e) {
			
			throw new BadCredentialsException(e.getMessage());
		}

		throw new BadCredentialsException("JWS verification failed!");
	}
}
