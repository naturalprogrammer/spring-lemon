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
	
	public static final String LEMON_IAT = "lemon-iat";
    public static final String AUTH_AUDIENCE = "auth";
    public static final String VERIFY_AUDIENCE = "verify";
    public static final String FORGOT_PASSWORD_AUDIENCE = "forgot-password";
	public static final String CHANGE_EMAIL_AUDIENCE = "change-email";
	
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


	public String createToken(String audience, String subject, Long expirationMillis) {

		return createToken(audience, subject, expirationMillis, new HashMap<>());
	}

	public JWTClaimsSet parseToken(String token, String audience) {

		try {
			
			JWTClaimsSet claims = jwtProcessor.process(token, null);
			LemonUtils.ensureAuthority(audience != null &&
					claims.getAudience().contains(audience),
						"com.naturalprogrammer.spring.wrong.audience");
			
			LemonUtils.ensureAuthority(claims.getExpirationTime().after(new Date()),
					"com.naturalprogrammer.spring.expiredToken");
			
			return claims;
			
		} catch (ParseException | BadJOSEException | JOSEException e) {

			throw new BadCredentialsException(e.getMessage());
		}
	}
	
	public JWTClaimsSet parseToken(String token, String audience, long issuedAfter) {
		
		JWTClaimsSet claims = parseToken(token, audience);
		
		long issueTime = (long) claims.getClaim(LEMON_IAT);
		LemonUtils.ensureAuthority(issueTime >= issuedAfter,
				"com.naturalprogrammer.spring.obsoleteToken");

		return claims;
	}	
	
	public void addAuthHeader(HttpServletResponse response, String username, Long expirationMillis) {
	
		response.addHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME,
				LemonSecurityConfig.TOKEN_PREFIX +
				createToken(AUTH_AUDIENCE, username, expirationMillis));
	}
}
