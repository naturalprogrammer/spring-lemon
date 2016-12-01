package com.naturalprogrammer.spring.lemon.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.naturalprogrammer.spring.lemon.LemonProperties;
import com.naturalprogrammer.spring.lemon.LemonProperties.Cors;

/**
 * A filter to facilitate CORS handling.
 * To disable this (e.g. while testing or in non-browser apps),
 * in your application.properties, don't provide
 * the <code>lemon.cors.allowedOrigins</code> property.
 * 
 * @author Sanjay Patel
 * @see <a href="https://spring.io/guides/gs/rest-service-cors/">Spring guide</a>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // needs to come first
@ConditionalOnProperty(name="lemon.cors.allowed-origins")
public class LemonCorsFilter extends OncePerRequestFilter {

	private final Log log = LogFactory.getLog(getClass());

	protected LemonProperties properties;
		
	@Autowired
	public void setProperties(LemonProperties properties) {
		this.properties = properties;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.debug("Inside LemonCorsFilter");
		
		Cors cors = properties.getCors(); 
				
		// origin as provided by the browser
		String origin = request.getHeader("Origin");
		
		// "*" is neither recommended, nor does it work
		// when $httpProvider.defaults.withCredentials = true;
		response.setHeader("Access-Control-Allow-Origin",
			ArrayUtils.contains(cors.getAllowedOrigins(), origin) ? // if origin is whitelisted,
			origin : properties.getApplicationUrl()); // use it, or else, return the application url
		
		// allowed methods
		response.setHeader("Access-Control-Allow-Methods",
			StringUtils.join(cors.getAllowedMethods(), ","));
		
		// allow headers
		response.setHeader("Access-Control-Allow-Headers",
			StringUtils.join(cors.getAllowedHeaders(), ","));

		// See http://stackoverflow.com/questions/25673089/why-is-access-control-expose-headers-needed#answer-25673446
		response.setHeader("Access-Control-Expose-Headers",
				StringUtils.join(cors.getExposedHeaders(), ","));

		// max age
		response.setHeader("Access-Control-Max-Age",
			Long.toString(cors.getMaxAge()));
		
		// needed when $httpProvider.defaults.withCredentials = true;
		response.setHeader("Access-Control-Allow-Credentials", "true"); 

		// Don't let OPTIONs pass.
		// Otherwise certain things like Spring Security
		// don't behave properly sometimes.
		// E.g., the SwitchUserFilter doesn't work. 
		if (!request.getMethod().equals("OPTIONS"))
			filterChain.doFilter(request, response);
	}
}
