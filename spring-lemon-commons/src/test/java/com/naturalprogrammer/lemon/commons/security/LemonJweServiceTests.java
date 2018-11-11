package com.naturalprogrammer.lemon.commons.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;

import com.naturalprogrammer.spring.lemon.commons.security.LemonJweService;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

public class LemonJweServiceTests {
	
	private static final Log log = LogFactory.getLog(LemonJweServiceTests.class);	

	// An aes-128-cbc key generated at https://asecuritysite.com/encryption/keygen (take the "key" field)
	private static final String SECRET1 = "926D96C90030DD58429D2751AC1BDBBC";
	private static final String SECRET2 = "538518AB685B514685DA8055C03DDA63";
		 
	private LemonJweService service1;
	private LemonJweService service2;

	public LemonJweServiceTests() throws KeyLengthException {
		
		service1 = new LemonJweService(SECRET1);
		service2 = new LemonJweService(SECRET2);
	}
	
	@Test
	public void testJwtParseToken() {
		
		log.info("Creating token ...");
		String token = service1.createToken("auth", "subject", 5000L,
				LecUtils.mapOf("username", "abc@example.com"));
		
		log.info("Parsing token ...");
		JWTClaimsSet claims = service1.parseToken(token, "auth");
		
		log.info("Parsed token.");
		Assert.assertEquals("subject", claims.getSubject());
		Assert.assertEquals("abc@example.com", claims.getClaim("username"));
	}
	
	@Test
	public void testJws() throws KeyLengthException, JOSEException, ParseException {
		
		// We need a 256-bit key for HS256 which must be pre-shared
		byte[] sharedKey = new byte[32];
		new SecureRandom().nextBytes(sharedKey);

		JWSSigner signer = new MACSigner(sharedKey);
		JWSVerifier verifier = new MACVerifier(sharedKey);

		
		log.info("Creating JWS token ...");		
		// Create an HMAC-protected JWS object with some payload
		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),
		                                    new Payload("Hello, world!"));

		// Apply the HMAC to the JWS object
		jwsObject.sign(signer);

		String token = jwsObject.serialize();
		
		// Output in URL-safe format
		log.info("Created token: " + token);
		
		// To parse the JWS and verify it, e.g. on client-side
		jwsObject = JWSObject.parse(token);
		assertTrue(jwsObject.verify(verifier));
		assertEquals("Hello, world!", jwsObject.getPayload().toString());
		log.info("Parsed token.");


	}

	@Test(expected = BadCredentialsException.class)
	public void testJwtParseTokenWrongAudience() {
		
		String token = service1.createToken("auth", "subject", 5000L);
		service1.parseToken(token, "auth2");
	}
	
	@Test(expected = BadCredentialsException.class)
	public void testJwtParseTokenExpired() throws InterruptedException {
		
		String token = service1.createToken("auth", "subject", 1L);
		Thread.sleep(1L);
		service1.parseToken(token, "auth");
	}
	
	@Test(expected = BadCredentialsException.class)
	public void testJwtParseTokenWrongSecret() {
		
		String token = service1.createToken("auth", "subject", 5000L);
		service2.parseToken(token, "auth");
	}

	@Test(expected = BadCredentialsException.class)
	public void testParseTokenCutoffTime() throws InterruptedException {

		String token = service1.createToken("auth", "subject", 5000L);
		Thread.sleep(1L);				
		service1.parseToken(token, "auth", System.currentTimeMillis());
	}
}
