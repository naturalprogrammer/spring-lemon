package com.naturalprogrammer.spring.lemon.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.CompositeFilter;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.LemonProperties.ClientResource;


/**
 * Security configuration class. Extend it in the
 * application, and make a configuration class. Override
 * protected methods, if you need any customization.
 * 
 * @author Sanjay Patel
 */
@EnableOAuth2Client
@EnableGlobalMethodSecurity(prePostEnabled = true)
public abstract class LemonSecurityConfig extends WebSecurityConfigurerAdapter {
	
	// Computed authorities
	public static final String GOOD_ADMIN = "GOOD_ADMIN";
	public static final String GOOD_USER = "GOOD_USER";
	
	// remember-me related
	public static final String REMEMBER_ME_COOKIE = "rememberMe";
	public static final String REMEMBER_ME_PARAMETER = "rememberMe";

	// CSRF related
	public static final String XSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";
	public static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";
	
	protected LemonProperties properties;
	protected UserDetailsService userDetailsService;
	protected AuthenticationSuccessHandler authenticationSuccessHandler;
	protected LogoutSuccessHandler logoutSuccessHandler;
	protected OAuth2ClientContext oauth2ClientContext;
	protected Map<String, LemonPrincipalExtractor> principalExtractors;
	
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
	
	@Autowired
	public void setOauth2ClientContext(OAuth2ClientContext oauth2ClientContext) {
		this.oauth2ClientContext = oauth2ClientContext;
	}
	
	@Autowired
	public void setPrincipalExtractor(Set<LemonPrincipalExtractor> principalExtractors) {
		this.principalExtractors = principalExtractors.stream().collect(
                Collectors.toMap(LemonPrincipalExtractor::getProvider, Function.identity()));;
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
		sso(http); // Social login configuration
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
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
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


	protected void sso(HttpSecurity http) {
		
		http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
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
	
	
	protected Filter ssoFilter() {
		
		  List<Filter> filters = properties.getClientResources()
				  .stream()
				  .map(this::ssoFilter)
				  .collect(Collectors.toList());

		  CompositeFilter filter = new CompositeFilter();		  
		  filter.setFilters(filters);
		  
		  return filter;
	}

	protected Filter ssoFilter(ClientResource client) {
		  
		OAuth2ClientAuthenticationProcessingFilter filter =
				new OAuth2ClientAuthenticationProcessingFilter("/login/" + client.getName());
		
		//SavedRequestAwareAuthenticationSuccessHandler successHandler =
		//	new SavedRequestAwareAuthenticationSuccessHandler("http://localhost:9000");
		SimpleUrlAuthenticationSuccessHandler successHandler =
				new SimpleUrlAuthenticationSuccessHandler(properties.getAfterOauth2LoginUrl());
		
		filter.setAuthenticationSuccessHandler(successHandler);
		  
		OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		filter.setRestTemplate(template);
		
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(
		    client.getResource().getUserInfoUri(), client.getClient().getClientId());
		
		PrincipalExtractor principalExtractor = principalExtractors.get(client.getName());
		if (principalExtractor == null)
			principalExtractor = principalExtractors.get(AbstractPrincipalExtractor.DEFAULT);
		
		tokenServices.setPrincipalExtractor(principalExtractor);
		tokenServices.setAuthoritiesExtractor(
				map -> (List<GrantedAuthority>) map.get(AbstractPrincipalExtractor.AUTHORITIES));
		tokenServices.setRestTemplate(template);
		filter.setTokenServices(tokenServices);
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
