//package com.naturalprogrammer.spring.lemon.security;
//
//import java.util.Date;
//
//import javax.servlet.http.HttpServletResponse;
//
//import com.naturalprogrammer.spring.lemon.LemonProperties;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.impl.TextCodec;
//
//public class OldJwtService {
//	
//	public static enum Audience {
//		
//		AUTH;
//	}
//
//    private byte[] secretKey;
//    
//	public OldJwtService(LemonProperties properties) {
//		
//		secretKey = TextCodec.BASE64.decode(properties.getJwt().getSecret());
//	}
//
//
//    public String getAuthToken(String username, Long expirationMilli) {
//		
//		return Jwts.builder()
//	        .setSubject(username)
//	        .setAudience(Audience.AUTH.name())
//	        .setExpiration(new Date(System.currentTimeMillis() + expirationMilli))
//	        .signWith(SignatureAlgorithm.HS256, secretKey)
//	        .compact();
//	}
//    
//	public void addJwtAuthHeader(HttpServletResponse response, String username, Long expirationMilli) {
//		
//        response.addHeader(LemonSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, LemonSecurityConfig.TOKEN_PREFIX + getAuthToken(username, expirationMilli));
//	}
//
//	public String parseAuthSubject(String token) {
//		
//		return Jwts.parser()
//		     .setSigningKey(secretKey)
//		     .requireAudience(Audience.AUTH.name())
//		     .parseClaimsJws(token)
//		     .getBody()
//		     .getSubject();
//	}
//
//
//	public Date parseIssuedAt(String token) {
//
//		Date issuedAt = Jwts.parser()
//			     .setSigningKey(secretKey)
//			     .parseClaimsJws(token)
//			     .getBody().getIssuedAt();
//		
//		assert issuedAt != null;
//		return issuedAt;
//	}
//}
