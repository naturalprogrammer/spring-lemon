package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 * 
 * @author Sanjay Patel
 */
public class OAuth2AuthenticationSuccessHandler
<U extends AbstractUser<U,ID>, ID extends Serializable>
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(OAuth2AuthenticationSuccessHandler.class);

	private LemonProperties properties;
	private JwtService jwtService;

	public OAuth2AuthenticationSuccessHandler(LemonProperties properties, JwtService jwtService) {

		this.properties = properties;
		this.jwtService = jwtService;

		log.info("Created");
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request,
			HttpServletResponse response) {
		
		SpringUser<ID> springUser = LemonUtils.getSpringUser();
		
		String shortLivedAuthToken = jwtService.createToken(
				JwtService.AUTH_AUDIENCE,
				springUser.getUsername(),
				(long) properties.getJwt().getShortLivedMillis());

		return properties.getApplicationUrl()
				+ "/social-login-success?token="
				+ shortLivedAuthToken;
		
//		return properties.getApplicationUrl()
//			+ "/users/" + springUser.getId()
//			+ "/social-login-success/" + springUser.getNonce();
	}
}
