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
 * If you want to disable this, e.g. while testing or in pure REST APIs,
 * in your application.properties, use
 * 
 * lemon.cors.enabled: false
 * 
 * https://spring.io/guides/gs/rest-service-cors/
 * 
 * @author Sanjay Patel
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name="lemon.cors.allowedOrigins")
public class SimpleCorsFilter extends OncePerRequestFilter {

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	LemonProperties properties;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.debug("Inside SimpleCorsFilter");
		
		Cors cors = properties.getCors(); 
				
		String origin = request.getHeader("Origin");
		
		String allowedOrigin = properties.getApplicationUrl();
		if (ArrayUtils.contains(cors.getAllowedOrigins(), origin))
			origin = allowedOrigin;
		
		// "*" is neither recommended, nor does it work 
		// when $httpProvider.defaults.withCredentials = true;
		response.setHeader("Access-Control-Allow-Origin",
			ArrayUtils.contains(cors.getAllowedOrigins(), origin) ?
			origin : properties.getApplicationUrl()); 
		
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

		filterChain.doFilter(request, response);
	}
}
