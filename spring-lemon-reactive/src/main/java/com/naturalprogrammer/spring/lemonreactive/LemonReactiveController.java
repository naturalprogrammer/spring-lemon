package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;

import reactor.core.publisher.Mono;

/**
 * The Lemon Mongo API. See the
 * <a href="https://github.com/naturalprogrammer/spring-lemon#documentation-and-resources">
 * API documentation</a> for details.
 * 
 * @author Sanjay Patel
 */
public class LemonReactiveController
	<U extends AbstractMongoUser<ID>, ID extends Serializable> {

	private static final Log log = LogFactory.getLog(LemonReactiveController.class);

	private LemonReactiveService<U, ID> lemonReactiveService;
	
	@Autowired
	public void createLemonController(LemonReactiveService<U, ID> lemonReactiveService) {
		
		this.lemonReactiveService = lemonReactiveService;		
		log.info("Created");
	}

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
	public Mono<UserDto<ID>> login() {
		
		log.debug("Returning current user ... ");
		return LerUtils.currentUser();
	}
	
	
	/**
	 * Signs up a user, and
	 * returns current-user data and an Authorization token as a response header.
	 */
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	protected Mono<U> signup(@RequestBody @JsonView(UserUtils.SignupInput.class)
			Mono<U> user,
			ServerWebExchange response) {
		
		log.debug("Signing up: " + user);
		return lemonReactiveService.signup(user);
		//log.debug("Signed up: " + user);

		//return Mono.empty();
		//return userWithToken(response);
	}
}
