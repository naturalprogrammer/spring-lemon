package com.naturalprogrammer.spring.lemon.commonsweb.security;

import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class. Extend it in the
 * application, and make a configuration class. Override
 * protected methods, if you need any customization.
 * 
 * @author Sanjay Patel
 */
public class LemonWebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final Log log = LogFactory.getLog(LemonWebSecurityConfig.class);

	protected BlueTokenService blueTokenService;
	
	@Autowired
	public void createLemonWebSecurityConfig(BlueTokenService blueTokenService) {

		this.blueTokenService = blueTokenService;		
		log.info("Created");
	}

	/**
	 * Security configuration, calling protected methods
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		sessionCreationPolicy(http); // set session creation policy
		logout(http); // logout related configuration
		exceptionHandling(http); // exception handling
		tokenAuthentication(http); // configure token authentication filter
		csrf(http); // CSRF configuration
		cors(http); // CORS configuration
		authorizeRequests(http); // authorize requests
		otherConfigurations(http); // override this to add more configurations
	}

	
	/**
	 * Configuring session creation policy
	 */
	protected void sessionCreationPolicy(HttpSecurity http) throws Exception {
		
		// No session
		http.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

		
	/**
	 * Logout related configuration
	 */
	protected void logout(HttpSecurity http) throws Exception {
		
		http
			.logout().disable(); // we are stateless; so /logout endpoint not needed			
	}

	
	/**
	 * Configures exception-handling
	 */
	protected void exceptionHandling(HttpSecurity http) throws Exception {
		
		http
		.exceptionHandling()
		
			/***********************************************
			 * To prevent redirection to the login page
			 * when someone tries to access a restricted page
			 **********************************************/
			.authenticationEntryPoint(new Http403ForbiddenEntryPoint());
	}


	/**
	 * Configuring token authentication filter
	 */
	protected void tokenAuthentication(HttpSecurity http) {
		
		http.addFilterBefore(new LemonCommonsWebTokenAuthenticationFilter(blueTokenService),
				UsernamePasswordAuthenticationFilter.class);
	}


	/**
	 * Disables CSRF. We are stateless.
	 */
	protected void csrf(HttpSecurity http) throws Exception {
		
		http
			.csrf().disable();
	}

	
	/**
	 * Configures CORS
	 */
	protected void cors(HttpSecurity http) throws Exception {
		
		http
			.cors();
	}

	
	/**
	 * URL based authorization configuration. Override this if needed.
	 */
	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.mvcMatchers("/**").permitAll();                  
	}
	

	/**
	 * Override this to add more http configurations,
	 * such as more authentication methods.
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void otherConfigurations(HttpSecurity http)  throws Exception {

	}
}
