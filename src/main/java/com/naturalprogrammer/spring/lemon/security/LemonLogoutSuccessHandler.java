//package com.naturalprogrammer.spring.lemon.security;
//
//import java.io.IOException;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
//
///**
// * Logout success handler for sending the response
// * to the client after successful logout. This would replace
// * the default handler of Spring Security that redirects the user
// * to the login page.
// * 
// * @author Sanjay Patel
// */
//public class LemonLogoutSuccessHandler
//	implements LogoutSuccessHandler {
//
//	private static final Log log = LogFactory.getLog(LemonLogoutSuccessHandler.class);
//
//	public LemonLogoutSuccessHandler() {
//		log.info("Created");
//	}
//
//	@Override
//	public void onLogoutSuccess(HttpServletRequest request,
//			HttpServletResponse response, Authentication authentication)
//			throws IOException, ServletException {
//
//    	response.setStatus(HttpServletResponse.SC_OK);
//    	log.debug("Logout succeeded.");
//	}
//}
