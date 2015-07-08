package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

@Component
public class LemonLogoutSuccessHandler
	implements LogoutSuccessHandler {

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
    private ObjectMapper objectMapper;

    @Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

    	response.setStatus(HttpServletResponse.SC_OK);
    	response.getOutputStream().print(
    			objectMapper.writeValueAsString(
    			LemonUtil.getBean(LemonService.class).userForClient()));
    	
        log.debug("Logout succeeded.");        
		
	}

}
