package com.naturalprogrammer.spring.lemon.security;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.naturalprogrammer.spring.lemon.LemonProperties;

/**
 * Security configuration class. Extend it in the
 * application, and make a configuration class. Override
 * protected methods, if you need any customization.
 * 
 * @author Sanjay Patel
 */
public class LemonSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final Log log = LogFactory.getLog(LemonPermissionEvaluator.class);

	// Computed authorities
	public static final String GOOD_ADMIN = "GOOD_ADMIN";
	public static final String GOOD_USER = "GOOD_USER";
	
	// CSRF related
	public static final String XSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";
	public static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";
	
	private LemonProperties properties;
	private UserDetailsService userDetailsService;
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	private AuthenticationFailureHandler authenticationFailureHandler;
	private LogoutSuccessHandler logoutSuccessHandler;
	private RememberMeServices rememberMeServices;
	private LemonTokenAuthenticationFilter<?, ?> lemonTokenAuthenticationFilter;
	private LemonOidcUserService oidcUserService;
	
	@Autowired
	public void createLemonSecurityConfig(LemonProperties properties, UserDetailsService userDetailsService,
			AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler,
			LogoutSuccessHandler logoutSuccessHandler, RememberMeServices rememberMeServices,
			LemonTokenAuthenticationFilter<?, ?> lemonTokenAuthenticationFilter,
			LemonOidcUserService oidcUserService) {

		this.properties = properties;
		this.userDetailsService = userDetailsService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
		this.logoutSuccessHandler = logoutSuccessHandler;
		this.rememberMeServices = rememberMeServices;
		this.lemonTokenAuthenticationFilter = lemonTokenAuthenticationFilter;
		this.oidcUserService = oidcUserService;
		
		log.info("Created");
	}
	

	/**
	 * Security configuration, calling protected methods
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		sessionCreationPolicy(http); // set session creation policy
		login(http); // authentication
		logout(http); // logout related configuration
		exceptionHandling(http); // exception handling
		rememberMe(http); // remember-me
		csrf(http); // csrf configuration
		switchUser(http); // switch-user configuration
		customTokenAuthentication(http); // API key authentication
		oauth2Client(http);
		authorizeRequests(http); // authorize requests
		otherConfigurations(http); // override this to add more configurations
	}


	/**
	 * Configuring Session creation policy
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void sessionCreationPolicy(HttpSecurity http) throws Exception {
		
		// Don't create a session; but if there's one, use it
		http.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.NEVER);
	}


	/**
	 * Configuring authentication.
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void login(HttpSecurity http) throws Exception {
		
		http
		.formLogin() // cookie based form login
			
			/******************************************
			 * Setting a successUrl would redirect the user there. Instead,
			 * let's send 200 and the userDto.
			 *****************************************/
			.successHandler(authenticationSuccessHandler)
			
			/*******************************************
			 * Setting the failureUrl will redirect the user to
			 * that url if login fails. Instead, we need to send
			 * 401. So, let's set failureHandler instead.
			 *******************************************/
        	.failureHandler(authenticationFailureHandler);
	}


	/**
	 * Logout related configuration
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void logout(HttpSecurity http) throws Exception {
		
		http
		.logout()
			
			/************************************************
			 * To prevent redirection to home page, we need to
			 * have this custom logoutSuccessHandler
			 ***********************************************/
			.logoutSuccessHandler(logoutSuccessHandler)
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID");
	}

	
	/**
	 * Configures exception-handling
	 * 
	 * @param http
	 * @throws Exception
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
	 * Configures remember-me
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void rememberMe(HttpSecurity http) throws Exception {
		
		http
			.rememberMe()
				.key(properties.getRememberMeKey())
				.rememberMeServices(rememberMeServices);
	}


	private final HashSet<String> csrfAllowedMethods = new HashSet<String>(
			Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS"));
	
	/**
	 * Configures CSRF
	 *  
	 * @param http
	 * @throws Exception
	 */
	protected void csrf(HttpSecurity http) throws Exception {
		
		http
			.csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.requireCsrfProtectionMatcher(request -> {
					
					if (csrfAllowedMethods.contains(request.getMethod()))
						return false;
						
					if (LemonTokenAuthenticationFilter.tokenPresent(request))
						return false;
					
					return true;
				});
	}

	
	/**
	 * Adds switch-user filter
	 * 
	 * @param http
	 */
	protected void switchUser(HttpSecurity http) {
		
		http
			.addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class);
	}


	private void customTokenAuthentication(HttpSecurity http) {
		http.addFilterBefore(lemonTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	private void oauth2Client(HttpSecurity http) throws Exception {
		
		http.oauth2Login()
			.defaultSuccessUrl(properties.getOauth2AuthenticationSuccessUrl(), true)
			.userInfoEndpoint()
				.oidcUserService(oidcUserService);
				//.userService(oidcUserService);
	}	

	/**
	 * URL based authorization configuration. Override this if needed.
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.mvcMatchers("/login/impersonate*").hasRole(GOOD_ADMIN)
			.mvcMatchers("/logout/impersonate*").authenticated()
			.mvcMatchers("/**").permitAll();                  
	}
	
	/**
	 * Returns switch-user filter
	 * 
	 * @return
	 */
	protected SwitchUserFilter switchUserFilter() {
		SwitchUserFilter filter = new SwitchUserFilter();
		filter.setUserDetailsService(userDetailsService);
		filter.setSuccessHandler(authenticationSuccessHandler);
		filter.setFailureHandler(authenticationFailureHandler);
		return filter;
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
