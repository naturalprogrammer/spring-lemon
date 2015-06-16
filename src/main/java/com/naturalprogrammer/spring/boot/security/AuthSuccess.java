package com.naturalprogrammer.spring.boot.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.boot.SaUtil;

@Component
public class AuthSuccess extends SimpleUrlAuthenticationSuccessHandler {
	
	private Log log = LogFactory.getLog(getClass());
	
    @Autowired
    private ObjectMapper objectMapper;
    
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // instead of this, the statement below is introduced: handle(request, response, authentication);
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.getOutputStream().print(objectMapper.writeValueAsString(SaUtil.getLoggedInUser().getUserDto()));
        clearAuthenticationAttributes(request);
        log.info("AuthenticationSuccess: " + SaUtil.getLoggedInUser().getUserDto());
        
    }
}
