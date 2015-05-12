package com.naturalprogrammer.spring.boot.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * https://spring.io/guides/gs/rest-service-cors/
 * 
 * @author skpat_000
 *
 */
@Component
public class SimpleCorsFilter implements Filter {

	@Value("${clientUrl: http://localhost:9000}")
	private String clientUrl;	
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", clientUrl); // "*" does not work when $httpProvider.defaults.withCredentials = true;
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with,origin,content-type,accept");
		response.setHeader("Access-Control-Allow-Credentials", "true"); // needed when $httpProvider.defaults.withCredentials = true;
		
		chain.doFilter(req, res);
	}

	public void init(FilterConfig filterConfig) {}

	public void destroy() {}
}
