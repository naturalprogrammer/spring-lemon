package com.naturalprogrammer.spring.lemon.security;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

/**
 * References:
 * 
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwe-with-shared-key
 * https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
 * 
 * @author Sanjay
 */
public class JwtService {
	
	public static enum Audience {
		
		AUTH;
	}

    private DirectEncrypter encrypter;
    private JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512);
    private ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor;
    
	public JwtService(String secret) throws KeyLengthException {
		
		byte[] secretKey = Base64.encode(secret).decode();
		encrypter = new DirectEncrypter(secretKey);
		jwtProcessor = new DefaultJWTProcessor<SimpleSecurityContext>();
		
		// The JWE key source
		JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<SimpleSecurityContext>(secretKey);

		// Configure a key selector to handle the decryption phase
		JWEKeySelector<SimpleSecurityContext> jweKeySelector =
				new JWEDecryptionKeySelector<SimpleSecurityContext>(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, jweKeySource);
		
		jwtProcessor.setJWEKeySelector(jweKeySelector);
	}

	public String createToken(Audience aud, String subject, Long expirationMilli, Map<String, Object> claimMap) {
		
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
		
		builder
    		.issueTime(new Date())
    		.expirationTime(new Date(System.currentTimeMillis() + expirationMilli))
    		.audience(aud.name())
    		.subject(subject);
		
		claimMap.forEach(builder::claim);
		
		JWTClaimsSet claims = builder.build();

    	Payload payload = new Payload(claims.toJSONObject());

    	// Create the JWE object and encrypt it
    	JWEObject jweObject = new JWEObject(header, payload);
    	
    	try {
    		
			jweObject.encrypt(encrypter);
			
		} catch (JOSEException e) {
			
			throw new RuntimeException(e);
		}

    	// Serialize to compact JOSE form...
    	return jweObject.serialize();
	}


	public String createToken(Audience auth, String subject, Long expirationMilli) {

		return createToken(auth, subject, expirationMilli, new HashMap<>());
	}

	public JWTClaimsSet parseToken(String token) {

		try {
			
			return jwtProcessor.process(token, null);
			
		} catch (ParseException | BadJOSEException | JOSEException e) {

			throw new RuntimeException(e);
		}
	}
	
	public void addAuthHeader(HttpServletResponse response, String username, Long expirationMilli) {
	
		response.addHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME,
				LemonSecurityConfig.TOKEN_PREFIX +
				createToken(Audience.AUTH, username, expirationMilli));
	}
}
