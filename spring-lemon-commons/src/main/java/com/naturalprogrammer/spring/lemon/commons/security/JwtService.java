package com.naturalprogrammer.spring.lemon.commons.security;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.BadCredentialsException;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
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
 * JWT Service
 * 
 * References:
 * 
 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwe-with-shared-key
 * https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
 */
public class JwtService {
	
	private static final Log log = LogFactory.getLog(JwtService.class);
	
	public static final String LEMON_IAT = "lemon-iat";
    public static final String AUTH_AUDIENCE = "auth";
    public static final String VERIFY_AUDIENCE = "verify";
    public static final String FORGOT_PASSWORD_AUDIENCE = "forgot-password";
	public static final String CHANGE_EMAIL_AUDIENCE = "change-email";
	public static final String SELF_SUFFICIENT = "sufficient";
	
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

	/**
	 * Creates a token
	 */
	public String createToken(String aud, String subject, Long expirationMillis, Map<String, Object> claimMap) {
		
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
		
		builder
    		//.issueTime(new Date())
    		.expirationTime(new Date(System.currentTimeMillis() + expirationMillis))
    		.audience(aud)
    		.subject(subject)
    		.claim(LEMON_IAT, System.currentTimeMillis());
		
		//claimMap.put("iat", new Date());
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

	/**
	 * Creates a token
	 */
	public String createToken(String audience, String subject, Long expirationMillis) {

		return createToken(audience, subject, expirationMillis, new HashMap<>());
	}

	/**
	 * Parses a token
	 */
	public JWTClaimsSet parseToken(String token, String audience) {

		try {
			
			JWTClaimsSet claims = jwtProcessor.process(token, null);
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
			
		} catch (ParseException | BadJOSEException | JOSEException e) {

			throw new BadCredentialsException(e.getMessage());
		}
	}
	
	/**
	 * Parses a token
	 */
	public JWTClaimsSet parseToken(String token, String audience, long issuedAfter) {
		
		JWTClaimsSet claims = parseToken(token, audience);
		
		long issueTime = (long) claims.getClaim(LEMON_IAT);
		LecUtils.ensureCredentials(issueTime >= issuedAfter,
				"com.naturalprogrammer.spring.obsoleteToken");

		return claims;
	}	
}
