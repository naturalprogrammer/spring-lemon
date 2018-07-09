package com.naturalprogrammer.spring.lemonreactive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;

import reactor.core.publisher.Mono;

/**
 * The Lemon Mongo API. See the
 * <a href="https://github.com/naturalprogrammer/spring-lemon#documentation-and-resources">
 * API documentation</a> for details.
 * 
 * @author Sanjay Patel
 */
public class LemonReactiveController {

	private static final Log log = LogFactory.getLog(LemonReactiveController.class);

	/**
	 * A simple function for pinging this server.
	 */
	@GetMapping("/ping")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void ping() {
		
		log.debug("Received a ping");
	}
	
	/**
	 * A simple function for pinging this server.
	 */
	@PostMapping("/login")
	public Mono<User> login() {
		
		log.debug("Returning current user ... ");
		return LerUtils.currentUser();
	}
}
