package com.naturalprogrammer.spring.lemon.security;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jwt.JWTClaimsSet;

public class JwtServiceTests {

	// An aes-128-cbc key generated at https://asecuritysite.com/encryption/keygen (take the "key" field)
	private static final String SECRET1 = "926D96C90030DD58429D2751AC1BDBBC";
	private static final String SECRET2 = "538518AB685B514685DA8055C03DDA63";
		 
	private JwtService service1;
	private JwtService service2;

	public JwtServiceTests() throws KeyLengthException {
		
		service1 = new JwtService(SECRET1);
		service2 = new JwtService(SECRET2);
	}
	
	@Test
	public void testJwtParseToken() {
		
		String token = service1.createToken("auth", "subject", 5000L,
				LemonUtils.mapOf("username", "abc@example.com"));
		JWTClaimsSet claims = service1.parseToken(token, "auth");
		
		Assert.assertEquals("subject", claims.getSubject());
		Assert.assertEquals("abc@example.com", claims.getClaim("username"));
	}

	@Test(expected = AccessDeniedException.class)
	public void testJwtParseTokenWrongAudience() {
		
		String token = service1.createToken("auth", "subject", 5000L);
		service1.parseToken(token, "auth2");
	}
	
	@Test(expected = AccessDeniedException.class)
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

	@Test(expected = AccessDeniedException.class)
	public void testParseTokenCutoffTime() throws InterruptedException {

		String token = service1.createToken("auth", "subject", 5000L);
		Thread.sleep(1001L);				
		service1.parseToken(token, "auth", new Date());
	}
}
