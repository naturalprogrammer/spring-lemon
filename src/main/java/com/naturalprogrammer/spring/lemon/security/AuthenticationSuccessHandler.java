package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Authentication success handler for sending the response
 * to the client after successful authentication. This would replace
 * the default handler of Spring Security
 * 
 * @author Sanjay Patel
 */
public class AuthenticationSuccessHandler
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(AuthenticationSuccessHandler.class);
	
    private ObjectMapper objectMapper;    
    private JwtService jwtService;
    private long defaultExpirationMillis;
    
	public AuthenticationSuccessHandler(ObjectMapper objectMapper, JwtService jwtService, LemonProperties properties) {
		
		this.objectMapper = objectMapper;
		this.jwtService = jwtService;
		this.defaultExpirationMillis = properties.getJwt().getExpirationMillis();
		
		log.info("Created");
	}

	
	@Override
    public void onAuthenticationSuccess(HttpServletRequest request,
    		HttpServletResponse response,
            Authentication authentication)
    throws IOException, ServletException {

        // Instead of handle(request, response, authentication),
		// the statements below are introduced
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    	String expirationMillisStr = request.getParameter("expirationMillis");
    	long expirationMillis = expirationMillisStr == null ?
    			defaultExpirationMillis : Long.valueOf(expirationMillisStr);
 
    	// get the current-user
    	SpringUser<?> currentUser = LemonUtils.getSpringUser();

    	jwtService.addAuthHeader(response, currentUser.getUsername(), expirationMillis);
    	
    	// write current-user data to the response  
    	response.getOutputStream().print(
    			objectMapper.writeValueAsString(currentUser));

    	// as done in the base class
    	clearAuthenticationAttributes(request);
        
        log.debug("Authentication succeeded for user: " + currentUser);        
    }
}
