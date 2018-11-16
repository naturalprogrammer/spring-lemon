package com.naturalprogrammer.spring.lemon.commons.security;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.BadCredentialsException;

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
 * JWE Service
 * 
 * References:
 * 
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwe-with-shared-key
 * https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
 */
public class LemonJweService extends AbstractJwtService implements GreenTokenService {
	
	private static final Log log = LogFactory.getLog(LemonJweService.class);
	
	private DirectEncrypter encrypter;
    private JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
    private ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor;
    
	public LemonJweService(String secret) throws KeyLengthException {
		
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


	@Override
	public String createToken(String aud, String subject, Long expirationMillis, Map<String, Object> claimMap) {
		
		Payload payload = createPayload(aud, subject, expirationMillis, claimMap);
				
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
	

	/**
	 * Parses a token
	 */
	protected JWTClaimsSet parseToken(String token) {
		
		try {
			
			return jwtProcessor.process(token, null);
			
		} catch (ParseException | BadJOSEException | JOSEException e) {
			
			throw new BadCredentialsException(e.getMessage());
		}
	}
}
