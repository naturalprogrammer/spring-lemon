package com.naturalprogrammer.spring.boot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private AuthSuccess authSuccess;
	
	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;
	
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
	
    @Autowired
    @Override
    protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
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
				.successHandler(authSuccess)
				
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
	        	.failureHandler(new SimpleUrlAuthenticationFailureHandler())
	        	.and()
			.logout()
			
				/************************************************
				 * To prevent redirection to home page, we need to
				 * have this custom logoutSuccessHandler
				 ***********************************************/
				.logoutSuccessHandler(logoutSuccessHandler)
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.and()
			.csrf().disable()
			.authorizeRequests()
				.antMatchers("/j_spring_security_switch_user*").hasRole("ADMIN")
				.antMatchers("/secure").authenticated()
				.antMatchers("/**").permitAll();                  
	}

}
