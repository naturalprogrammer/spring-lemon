package com.naturalprogrammer.spring.lemon.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

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
	
	// JWT Token related
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_REQUEST_HEADER = "Authorization";
    public static final String TOKEN_RESPONSE_HEADER_NAME = "Lemon-Authorization";
    
	private UserDetailsService userDetailsService;
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	private AuthenticationFailureHandler authenticationFailureHandler;
	//private LogoutSuccessHandler logoutSuccessHandler;
	private LemonOidcUserService oidcUserService;
	private LemonOAuth2UserService<?, ?> oauth2UserService;
	private OAuth2AuthenticationSuccessHandler<?,?> oauth2AuthenticationSuccessHandler;
	private JwtAuthenticationProvider<?,?> jwtAuthenticationProvider;
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public void createLemonSecurityConfig(LemonProperties properties, UserDetailsService userDetailsService,
			AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler,
			//LogoutSuccessHandler logoutSuccessHandler,
			LemonOidcUserService oidcUserService,
			LemonOAuth2UserService<?, ?> oauth2UserService,
			OAuth2AuthenticationSuccessHandler<?,?> oauth2AuthenticationSuccessHandler,
			JwtAuthenticationProvider<?,?> jwtAuthenticationProvider,
			PasswordEncoder passwordEncoder
			) {

		this.userDetailsService = userDetailsService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
		//this.logoutSuccessHandler = logoutSuccessHandler;
		this.oidcUserService = oidcUserService;
		this.oauth2UserService = oauth2UserService;
		this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
		this.jwtAuthenticationProvider = jwtAuthenticationProvider;
		this.passwordEncoder = passwordEncoder;
		
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
		csrf(http); // csrf configuration
		//switchUser(http); // switch-user configuration
		//customTokenAuthentication(http); // API key authentication
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
		
		// No session
		http.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
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
		.logout().disable(); // we are stateless; so /logout endpoint not needed
			
//			/************************************************
//			 * To prevent redirection to home page, we need to
//			 * have this custom logoutSuccessHandler
//			 ***********************************************/
//			.logoutSuccessHandler(logoutSuccessHandler);
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
	 * Configures CSRF
	 *  
	 * @param http
	 * @throws Exception
	 */
	protected void csrf(HttpSecurity http) throws Exception {
		
		http
			.csrf().disable();
	}

	
//	/**
//	 * Adds switch-user filter
//	 * 
//	 * @param http
//	 */
//	protected void switchUser(HttpSecurity http) {
//		
//		http
//			.addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class);
//	}
//
//
//	private void customTokenAuthentication(HttpSecurity http) throws Exception {
//	
//		http.addFilterBefore(lemonTokenAuthenticationFilter(),
//				UsernamePasswordAuthenticationFilter.class);
//	}
	
	private void oauth2Client(HttpSecurity http) throws Exception {
		
		http.oauth2Login()
			.authorizationEndpoint()
				.authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository()).and()
			.successHandler(oauth2AuthenticationSuccessHandler)
			.userInfoEndpoint()
				.oidcUserService(oidcUserService)
				.userService(oauth2UserService);
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
	
//	/**
//	 * Returns switch-user filter
//	 * 
//	 * @return
//	 */
//	protected SwitchUserFilter switchUserFilter() {
//		
//		SwitchUserFilter filter = new SwitchUserFilter();
//		filter.setUserDetailsService(userDetailsService);
//		filter.setSuccessHandler(authenticationSuccessHandler);
//		filter.setFailureHandler(authenticationFailureHandler);
//		return filter;
//	}	
	
	/**
	 * Override this to add more http configurations,
	 * such as more authentication methods.
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void otherConfigurations(HttpSecurity http)  throws Exception {

	}

	/**
	 * Needed for configuring JwtAuthenticationProvider
	 */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  
        auth.userDetailsService(userDetailsService)
        	.passwordEncoder(passwordEncoder).and()
        	.authenticationProvider(jwtAuthenticationProvider);
    }

	@Bean
	@ConditionalOnMissingBean(LemonTokenAuthenticationFilter.class)
	public LemonTokenAuthenticationFilter lemonTokenAuthenticationFilter() throws Exception {
		
		return new LemonTokenAuthenticationFilter(super.authenticationManager());
	}
}
