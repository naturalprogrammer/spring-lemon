package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

@Component
public class AuthenticationSuccessHandler
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private final Log log = LogFactory.getLog(getClass());
	
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private LemonService<?,?> lemonService;
    
	@Override
    public void onAuthenticationSuccess(HttpServletRequest request,
    		HttpServletResponse response,
            Authentication authentication)
    throws IOException, ServletException {

//		AbstractUser<?,?> currentUser =
//			LemonUtil.getBean(LemonService.class)
//				.userForClient();
		
		AbstractUser<?,?> currentUser = lemonService.userForClient();
		
        // instead of this, the statement below is introduced: handle(request, response, authentication);
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    	response.getOutputStream().print(
    			objectMapper.writeValueAsString(currentUser));
        clearAuthenticationAttributes(request);
        
        log.debug("Authentication succeeded for user: " + currentUser);        
    }
}
