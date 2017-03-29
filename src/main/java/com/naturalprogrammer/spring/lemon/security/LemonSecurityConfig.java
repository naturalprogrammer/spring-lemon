package com.naturalprogrammer.spring.lemon.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.CompositeFilter;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.LemonProperties.RemoteResource;
import com.naturalprogrammer.spring.lemon.security.principalextractors.AbstractPrincipalExtractor;
import com.naturalprogrammer.spring.lemon.security.principalextractors.LemonPrincipalExtractor;

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
	private OAuth2ClientContext oauth2ClientContext;
	private Map<String, LemonPrincipalExtractor> principalExtractors;
	private LemonTokenAuthenticationFilter<?, ?> lemonTokenAuthenticationFilter;
	
	@Autowired
	public void createLemonSecurityConfig(LemonProperties properties, UserDetailsService userDetailsService,
			AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler,
			LogoutSuccessHandler logoutSuccessHandler, RememberMeServices rememberMeServices,
			OAuth2ClientContext oauth2ClientContext, Set<LemonPrincipalExtractor> principalExtractors,
			LemonTokenAuthenticationFilter<?, ?> lemonTokenAuthenticationFilter) {

		this.properties = properties;
		this.userDetailsService = userDetailsService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
		this.logoutSuccessHandler = logoutSuccessHandler;
		this.rememberMeServices = rememberMeServices;
		this.oauth2ClientContext = oauth2ClientContext;
		this.principalExtractors = principalExtractors.stream().collect(
              Collectors.toMap(LemonPrincipalExtractor::getProvider, Function.identity()));
		this.lemonTokenAuthenticationFilter = lemonTokenAuthenticationFilter;
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
		sso(http); // Social login configuration
		customToken(http); // Custom token authentication
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


	protected void sso(HttpSecurity http) {
		
		List<RemoteResource> clientResources = properties.getRemoteResources();
		
		if (clientResources != null && clientResources.size() > 0)
			http.addFilterBefore(socialAuthenticationFilter(properties.getRemoteResources()), BasicAuthenticationFilter.class);
	}


	private void customToken(HttpSecurity http) {
		http.addFilterBefore(lemonTokenAuthenticationFilter, BasicAuthenticationFilter.class);
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
	
	
	protected Filter socialAuthenticationFilter(List<RemoteResource> clientResources) {
		
		List<Filter> filters = properties.getRemoteResources()
				.stream()
				.map(this::oauth2AuthenticationFilter)
				.collect(Collectors.toList());

		CompositeFilter filter = new CompositeFilter();		  
		filter.setFilters(filters);
		  
		return filter;
	}

	protected Filter oauth2AuthenticationFilter(RemoteResource resource) {
		  
		OAuth2ClientAuthenticationProcessingFilter filter =
			new OAuth2ClientAuthenticationProcessingFilter("/login/" + resource.getId());
		
		SimpleUrlAuthenticationSuccessHandler successHandler =
			new SimpleUrlAuthenticationSuccessHandler(properties.getOauth2AuthenticationSuccessUrl());
		
		filter.setAuthenticationSuccessHandler(successHandler);
		filter.setRememberMeServices(rememberMeServices);
		  
		OAuth2RestTemplate template = new OAuth2RestTemplate(resource.getDetails(), oauth2ClientContext);
		filter.setRestTemplate(template);
		
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(
		    resource.getUserInfoUri(), resource.getDetails().getClientId());
		
		PrincipalExtractor principalExtractor = principalExtractors.get(resource.getId());
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
