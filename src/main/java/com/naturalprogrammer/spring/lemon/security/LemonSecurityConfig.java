package com.naturalprogrammer.spring.lemon.security;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.naturalprogrammer.spring.lemon.LemonProperties;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public abstract class LemonSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private LemonProperties properties;
	
	private static final String REMEMBER_ME_COOKIE = "rememberMe";
	private static final String REMEMBER_ME_PARAMETER = "rememberMe";
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	
	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;
	
	@Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
    	return new SimpleUrlAuthenticationFailureHandler();
    }	
	
	@Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
	
    /**
     * In case you don't want use the email field as the login id,
     * you may want to have a different userDetailsServices, and
     * override this method for injecting that here.
     */
	@Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			.formLogin()
//				.loginPage("/login")
//				.permitAll()
				
				/******************************************
				 * Setting a successUrl would redirect the user there. Instead,
				 * let's send 200 and the userDto.
				 *****************************************/
				.successHandler(authenticationSuccessHandler)
				
				/*******************************************
				 * Setting the failureUrl will redirect the user to
				 * that url if login fails. Instead, we need to send
				 * 401. So, let's set failureHandler instead.
				 * 
				 * Debug org.apache.catalina.core.StandardHostValve's
				 * private void status(Request request, Response response)
				 * if you want to understand why we get in the log. It's not a problem, as I understand.
				 * 2015-05-06 13:56:26.908 DEBUG 10184 --- [nio-8080-exec-1] o.s.b.a.e.mvc.EndpointHandlerMapping     : Did not find handler method for [/error]
				 * 2015-05-06 13:56:45.007 DEBUG 10184 --- [nio-8080-exec-3] o.s.b.a.e.mvc.EndpointHandlerMapping     : Looking up handler method for path /error2
				 *******************************************/
	        	.failureHandler(authenticationFailureHandler())
	        	.and()
			.logout()
			
				/************************************************
				 * To prevent redirection to home page, we need to
				 * have this custom logoutSuccessHandler
				 ***********************************************/
				.logoutSuccessHandler(logoutSuccessHandler)
				//.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID", REMEMBER_ME_COOKIE)
				.and()
				
			.exceptionHandling()
			
				/***********************************************
				 * To prevent redirection to the login page
				 * when someone tries to access a restricted page
				 **********************************************/
				.authenticationEntryPoint(new Http403ForbiddenEntryPoint())
				.and()
			.rememberMe()
				.key(properties.getRememberMeKey())
				.rememberMeServices(rememberMeServices())
				.and()
			//.csrf().disable()
			.csrf()
				.csrfTokenRepository(csrfTokenRepository())
				.and()
			.addFilterAfter(csrfCookieFilter(), CsrfFilter.class)
			.addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class);
		
		authorizeRequests(http);
	}

	protected void authorizeRequests(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/login/impersonate*").hasRole("ADMIN")
			.antMatchers("/logout/impersonate*").authenticated()
			.antMatchers("/only-for-admin*").hasRole("ADMIN")
			//.antMatchers("/secure").authenticated()
			.antMatchers("/**").permitAll();                  
	}
	
	/**
	 * Override this method if you want to 
	 * setup a different RememberMeServices
	 * 
	 * @return
	 */
    protected RememberMeServices rememberMeServices() {
    	
        TokenBasedRememberMeServices rememberMeServices =
        	new TokenBasedRememberMeServices
        		(properties.getRememberMeKey(), userDetailsService);
        rememberMeServices.setParameter(REMEMBER_ME_PARAMETER); // default is "remember-me" (in earlier spring security versions it was "_spring_security_remember_me")
        rememberMeServices.setCookieName(REMEMBER_ME_COOKIE);
        return rememberMeServices;
        
    }
    
	/**
	 * Makes it compatible to AngularJS CSRF token header name.
	 * Override this if you want to change the 
	 * header name.
	 *  
	 * @return
	 */
	protected CsrfTokenRepository csrfTokenRepository() {
		
		HttpSessionCsrfTokenRepository repository =
				new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}

	
	protected SwitchUserFilter switchUserFilter() {
		SwitchUserFilter filter = new SwitchUserFilter();
		filter.setUserDetailsService(userDetailsService);
		filter.setSuccessHandler(authenticationSuccessHandler);
		filter.setFailureHandler(authenticationFailureHandler());
		return filter;
	}
	
	protected Filter csrfCookieFilter() {
		return new CsrfCookieFilter();
	}

}
