package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a nonce-contained URL
 * 
 * @author Sanjay Patel
 */
public class OAuth2AuthenticationSuccessHandler
<U extends AbstractUser<U,ID>, ID extends Serializable>
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(OAuth2AuthenticationSuccessHandler.class);

	private LemonProperties properties;
	private AbstractUserRepository<U,ID> userRepository;

	public OAuth2AuthenticationSuccessHandler(LemonProperties properties) {

		this.properties = properties;

		log.info("Created");
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request,
			HttpServletResponse response) {
		
		SpringUser<ID> springUser = LemonUtils.getSpringUser();
				
		return properties.getApplicationUrl()
			+ "/users/" + springUser.getId()
			+ "/social-login-success/" + springUser.getNonce();
	}
}
