package com.naturalprogrammer.spring.lemon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authentication success handler for sending the response
 * to the client after successful authentication.
 * 
 * @author Sanjay Patel
 */
public class LemonAuthenticationSuccessHandler
	extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Log log = LogFactory.getLog(LemonAuthenticationSuccessHandler.class);
	
    private ObjectMapper objectMapper;    
    private LemonService<?, ?> lemonService;
    private long defaultExpirationMillis;
    
	public LemonAuthenticationSuccessHandler(ObjectMapper objectMapper, LemonService<?, ?> lemonService, LemonProperties properties) {
		
		this.objectMapper = objectMapper;
		this.lemonService = lemonService;
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
    	UserDto currentUser = LecwUtils.currentUser();

    	lemonService.addAuthHeader(response, currentUser.getUsername(), expirationMillis);
    	
    	// write current-user data to the response  
    	response.getOutputStream().print(
    			objectMapper.writeValueAsString(currentUser));

    	// as done in the base class
    	clearAuthenticationAttributes(request);
        
        log.debug("Authentication succeeded for user: " + currentUser);        
    }
}
