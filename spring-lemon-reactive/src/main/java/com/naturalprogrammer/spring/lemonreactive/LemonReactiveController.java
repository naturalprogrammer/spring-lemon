package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
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

    private long jwtExpirationMillis;
    private JwtService jwtService;
	private LemonReactiveService<U, ID> lemonReactiveService;
	
	
	@Autowired
	public void createLemonController(
			LemonProperties properties,
			LemonReactiveService<U, ID> lemonReactiveService,
			JwtService jwtService) {
		
		this.jwtExpirationMillis = properties.getJwt().getExpirationMillis();
		this.lemonReactiveService = lemonReactiveService;		
		this.jwtService = jwtService;

		log.info("Created");
	}

	
	/**
	 * A simple function for pinging this server.
	 */
	@PostMapping("/login")
	public Mono<UserDto<ID>> login(ServerWebExchange exchange) {
		
		log.debug("Returning current user ... ");
		long expirationMillis = exchange.getAttributeOrDefault("expirationMillis", jwtExpirationMillis);
		return userWithToken(exchange.getResponse(), expirationMillis);
	}

	
	/**
	 * A simple function for pinging this server.
	 */
	@GetMapping("/ping")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> ping() {
		
		log.debug("Received a ping");
		return Mono.empty();
	}

	
	/**
	 * Returns context properties needed at the client side,
	 * current-user data and an Authorization token as a response header.
	 */
	@GetMapping("/context")
	public Mono<Map<String, Object>> getContext(
			@RequestParam Optional<Long> expirationMillis,
			ServerHttpResponse response) {

		log.debug("Getting context ");
		
		return lemonReactiveService
			.getContext(expirationMillis, response)
			.doOnNext(context -> {
				log.debug("Returning context " + context);
			});
	}

	
	/**
	 * Signs up a user, and
	 * returns current-user data and an Authorization token as a response header.
	 */
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	protected Mono<UserDto<ID>> signup(Mono<U> user, ServerHttpResponse response) {
		
		log.debug("Signing up: " + user);
		
		return lemonReactiveService
			.signup(user)
			.doOnSuccess(userDto -> lemonReactiveService
				.addAuthHeader(response, userDto.getUsername(), jwtExpirationMillis));
	}

	
	/**
	 * Resends verification mail
	 */
	@PostMapping("/users/{id}/resend-verification-mail")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> resendVerificationMail(@PathVariable("id") ID userId) {
		
		log.debug("Resending verification mail for user " + userId);
		
		//Mono<U> user = lemonReactiveService.findUserById(id);
		return lemonReactiveService.resendVerificationMail(userId);
	}	

	/**
	 * returns the current user and a new authorization token in the response
	 */
	protected Mono<UserDto<ID>> userWithToken(ServerHttpResponse response) {
		
		return userWithToken(response, jwtExpirationMillis);
	}

	
	/**
	 * returns the current user and a new authorization token in the response
	 */
	protected Mono<UserDto<ID>> userWithToken(ServerHttpResponse response, long expirationMillis) {

		Mono<Optional<UserDto<ID>>> currentUser = LerUtils.currentUser();
		return currentUser.map(optionalUser -> optionalUser.orElseThrow(LexUtils.notFoundSupplier()))
			.doOnNext(user -> {
				log.debug("Adding auth header for " + user.getUsername());
				lemonReactiveService.addAuthHeader(response, user.getUsername(), expirationMillis);
			});
	}
}
