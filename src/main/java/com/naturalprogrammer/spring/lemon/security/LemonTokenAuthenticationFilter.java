package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.GenericFilterBean;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

public abstract class LemonTokenAuthenticationFilter
	<U extends AbstractUser<U,ID>, ID extends Serializable>
	extends GenericFilterBean {
	
    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);
	
	private PasswordEncoder passwordEncoder;
	private AbstractUserRepository<U, ID> userRepository;
	
	private String tokenSplitter = ":";
	
	public LemonTokenAuthenticationFilter() {
		log.info("Created");
	}
	
	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		
		log.info("Setting passwordEncoder");
		this.passwordEncoder = passwordEncoder;
	}

	public void setUserRepository(AbstractUserRepository<U, ID> userRepository) {
		
		log.info("Setting userRepository");
		this.userRepository = userRepository;
	}

	public static boolean tokenPresent(HttpServletRequest request) {
		
		String header = request.getHeader("Authorization");		
		return header != null && header.startsWith("Bearer ");
	}	

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		log.debug("Inside LemonTokenAuthenticationFilter ...");
		
		HttpServletRequest req = (HttpServletRequest) request;
		
		if (tokenPresent(req)) {
			
			log.debug("Found a token");
			
		    String tokenStr = req.getHeader("Authorization").substring(7);
		    String[] tokenParts = tokenStr.split(tokenSplitter);
		    
		    String id = tokenParts[0];
		    String token = tokenParts[1];
		    
			log.debug("Trying to get user " + id);

			U user = userRepository.findOne(parseId(id));
			
		    if (user == null)
		    	throw new BadCredentialsException(LemonUtil.getMessage("com.naturalprogrammer.spring.userNotFound"));

			log.debug("Trying to match the token");

			if (!passwordEncoder.matches(token, user.getAuthenticationToken()))
		    	throw new BadCredentialsException(LemonUtil.getMessage("com.naturalprogrammer.spring.wrong.authenticationToken"));
		    
		    user.decorate();
		    
		    SecurityContextHolder.getContext().setAuthentication(
			    	new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
			    
			log.debug("Token authentication successful");
			    
		} else
		
			log.debug("Token authentication skipped");
		
		chain.doFilter(request, response);
	}

	abstract protected ID parseId(String id);
	
	public String getTokenSplitter() {
		return tokenSplitter;
	}

	public void setTokenSplitter(String tokenSplitter) {
		this.tokenSplitter = tokenSplitter;
	}
}
