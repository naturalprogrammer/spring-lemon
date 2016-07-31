package com.naturalprogrammer.spring.lemon.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.naturalprogrammer.spring.lemon.LemonProperties;


/**
 * Security configuration class. Extend it in the
 * application, and make a configuration class. Override
 * protected methods, if you need any customization.
 * 
 * @author Sanjay Patel
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public abstract class LemonSecurityConfig extends WebSecurityConfigurerAdapter {
	
	// Computed authorities
	public static final String GOOD_ADMIN = "GOOD_ADMIN";
	public static final String GOOD_USER = "GOOD_USER";
	
	// remember-me related
	public static final String REMEMBER_ME_COOKIE = "rememberMe";
	public static final String REMEMBER_ME_PARAMETER = "rememberMe";
	
	protected LemonProperties properties;
	protected UserDetailsService userDetailsService;
	protected AuthenticationSuccessHandler authenticationSuccessHandler;
	protected LogoutSuccessHandler logoutSuccessHandler;
	
	@Autowired
	public void setProperties(LemonProperties properties) {
		this.properties = properties;
	}

	@Autowired
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Autowired
	public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	@Autowired
	public void setLogoutSuccessHandler(LogoutSuccessHandler logoutSuccessHandler) {
		this.logoutSuccessHandler = logoutSuccessHandler;
	}

	/**
	 * Authentication failure handler, to override the default behavior
	 * of spring security -  redirecting to the login screen 
	 */
	@Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
    	return new SimpleUrlAuthenticationFailureHandler();
    }	
	
	/**
	 * Password encoder
	 */
	@Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
	
//    /**
//     * In case you don't want use the email field as the login id,
//     * you may want to have a different userDetailsServices, and
//     * override this method for injecting that here.
//     */
//	@Override
//    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
//        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//    }
//	
	/**
	 * Security configuration, calling protected methods
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		login(http); // authentication
		logout(http); // logout related configuration
		exceptionHandling(http); // exception handling
		rememberMe(http); // remember-me
		csrf(http); // csrf configuration
		switchUser(http); // switch-user configuration
		authorizeRequests(http); // authorize requests
		otherConfigurations(http); // override this to add more configurations
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
        	.failureHandler(authenticationFailureHandler());
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
				.rememberMeServices(rememberMeServices());
	}

	
	/**
	 * Configures CSRF
	 *  
	 * @param http
	 * @throws Exception
	 */
	protected void csrf(HttpSecurity http) throws Exception {
		
		http
			.csrf()
				.csrfTokenRepository(csrfTokenRepository())
				.and()
			.addFilterAfter(new LemonCsrfFilter(), CsrfFilter.class);
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
	 * Override this to add more http configurations,
	 * such as more authentication methods.
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void otherConfigurations(HttpSecurity http)  throws Exception {

	}

	
	/**
	 * Override this method if you want to 
	 * setup a different RememberMeServices
	 * 
	 * @return
	 */
	protected AbstractRememberMeServices rememberMeServices() {
    	
        TokenBasedRememberMeServices rememberMeServices =
        	new TokenBasedRememberMeServices
        		(properties.getRememberMeKey(), userDetailsService);
        rememberMeServices.setParameter(REMEMBER_ME_PARAMETER); // default is "remember-me" (in earlier spring security versions it was "_spring_security_remember_me")
        rememberMeServices.setCookieName(REMEMBER_ME_COOKIE);
        return rememberMeServices;
        
    }
    

	/**
	 * returns a CookieCsrfTokenRepository, to remain
	 * compatible with AngularJS CSRF token header name.
	 * Override this if you want to change the 
	 * header name.
	 *  
	 * @return
	 */
	protected CsrfTokenRepository csrfTokenRepository() {
		
		//return new CookieCsrfTokenRepository();
		HttpSessionCsrfTokenRepository repository =
				new HttpSessionCsrfTokenRepository();
		repository.setHeaderName(LemonCsrfFilter.XSRF_TOKEN_HEADER_NAME);
		return repository;
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
		filter.setFailureHandler(authenticationFailureHandler());
		return filter;
	}	
}
