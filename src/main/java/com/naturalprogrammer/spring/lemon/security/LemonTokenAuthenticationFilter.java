package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

public abstract class LemonTokenAuthenticationFilter
	<U extends AbstractUser<U,ID>, ID extends Serializable>
	extends AbstractAuthenticationProcessingFilter {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AbstractUserRepository<U, ID> userRepository;
	
	@Autowired
	public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
		super.setAuthenticationSuccessHandler(authenticationSuccessHandler);
	}
	
	@Autowired
	public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
		super.setAuthenticationFailureHandler(authenticationFailureHandler);
	}
		
	public static boolean tokenPresent(HttpServletRequest request) {
		
		String header = request.getHeader("Authorization");		
		return header != null && header.startsWith("Bearer ");
	}
	
	public LemonTokenAuthenticationFilter() {
		
		super(request -> tokenPresent(request));
		setAuthenticationManager(new NoopAuthenticationManager());
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		
	    String tokenStr = request.getHeader("Authorization").substring(7);
	    String[] tokenVal = tokenStr.split(tokenSplitter());
	    
	    U user = userRepository.findOne(parseId(tokenVal[0]));
	    if (user == null)
	    	throw new BadCredentialsException(LemonUtil.getMessage("com.naturalprogrammer.spring.userNotFound"));
	    
	    if (!passwordEncoder.matches(tokenVal[1], user.getAuthenticationToken()))
	    	throw new BadCredentialsException(LemonUtil.getMessage("com.naturalprogrammer.spring.wrong.authenticationToken"));
	    
	    user.decorate();
	    UsernamePasswordAuthenticationToken authentication =
	    		new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
	    
		return authentication;
	}

	protected String tokenSplitter() {
		return ":";
	}

	abstract protected ID parseId(String id);
	
	private static class NoopAuthenticationManager implements AuthenticationManager {

		@Override
		public Authentication authenticate(Authentication authentication)
				throws AuthenticationException {
			throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
		}		
	}

}
