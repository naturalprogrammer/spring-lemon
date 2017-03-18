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

import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

public class LemonTokenAuthenticationFilter
	<U extends AbstractUser<U,ID>, ID extends Serializable>
	extends GenericFilterBean {
	
    private static final Log log = LogFactory.getLog(LemonTokenAuthenticationFilter.class);
	
	private PasswordEncoder passwordEncoder;
	private AbstractUserRepository<U, ID> userRepository;
	private LemonService<U,ID> lemonService;
	
	private String tokenSplitter = ":";
	
	public LemonTokenAuthenticationFilter(PasswordEncoder passwordEncoder,
			AbstractUserRepository<U, ID> userRepository,
			LemonService<U,ID> lemonService) {
		
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.lemonService = lemonService;
		log.info("Created");
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

			ID userId = lemonService.parseId(id);
			U user = userRepository.findOne(userId);
			
		    if (user == null)
		    	throw new BadCredentialsException(LemonUtil.getMessage("com.naturalprogrammer.spring.userNotFound"));

			log.debug("Trying to match the token");

			if (!passwordEncoder.matches(token, user.getApiKey()))
		    	throw new BadCredentialsException(LemonUtil.getMessage("com.naturalprogrammer.spring.wrong.authenticationToken"));
		    
		    user.decorate();
		    
		    SecurityContextHolder.getContext().setAuthentication(
			    	new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
			    
			log.debug("Token authentication successful");
			    
		} else
		
			log.debug("Token authentication skipped");
		
		chain.doFilter(request, response);
	}

	public String getTokenSplitter() {
		return tokenSplitter;
	}

	public void setTokenSplitter(String tokenSplitter) {
		this.tokenSplitter = tokenSplitter;
	}
}
