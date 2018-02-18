package com.naturalprogrammer.spring.lemon.security;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;

import com.naturalprogrammer.spring.lemon.util.LemonUtils;
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
	
    private DirectEncrypter encrypter;
    private JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
    private ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor;
    
	public JwtService(String secret) throws KeyLengthException {
		
		byte[] secretKey = secret.getBytes();
		encrypter = new DirectEncrypter(secretKey);
		jwtProcessor = new DefaultJWTProcessor<SimpleSecurityContext>();
		
		// The JWE key source
		JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<SimpleSecurityContext>(secretKey);

		// Configure a key selector to handle the decryption phase
		JWEKeySelector<SimpleSecurityContext> jweKeySelector =
				new JWEDecryptionKeySelector<SimpleSecurityContext>(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256, jweKeySource);
		
		jwtProcessor.setJWEKeySelector(jweKeySelector);
	}

	public String createToken(String aud, String subject, Long expirationMilli, Map<String, Object> claimMap) {
		
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
		
		builder
    		.issueTime(new Date())
    		.expirationTime(new Date(System.currentTimeMillis() + expirationMilli))
    		.audience(aud)
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


	public String createToken(String audience, String subject, Long expirationMilli) {

		return createToken(audience, subject, expirationMilli, new HashMap<>());
	}

	public JWTClaimsSet parseToken(String token, String audience) {

		try {
			
			JWTClaimsSet claims = jwtProcessor.process(token, null);
			LemonUtils.validateCredentials(claims.getAudience().contains(audience), "Wrong audience");
			LemonUtils.validateCredentials(claims.getExpirationTime().after(new Date()), "Token expired");
			
			return claims;
			
		} catch (ParseException | BadJOSEException | JOSEException e) {

			throw new BadCredentialsException(e.getMessage());
		}
	}
	
	public void addAuthHeader(HttpServletResponse response, String username, Long expirationMilli) {
	
		response.addHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME,
				LemonSecurityConfig.TOKEN_PREFIX +
				createToken(LemonSecurityConfig.AUTH_AUDIENCE, username, expirationMilli));
	}
}
