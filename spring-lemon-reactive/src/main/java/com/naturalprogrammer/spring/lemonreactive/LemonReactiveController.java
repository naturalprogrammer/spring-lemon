package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
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
		
		Mono<Optional<UserDto<ID>>> currentUser = LerUtils.currentUser();
		return lemonReactiveService.userWithToken(
				currentUser.map(Optional::get), exchange.getResponse(), expirationMillis);
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
		
		return userWithToken(lemonReactiveService.signup(user), response);
	}

	
	/**
	 * Resends verification mail
	 */
	@PostMapping("/users/{id}/resend-verification-mail")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> resendVerificationMail(@PathVariable("id") ID userId) {
		
		log.debug("Resending verification mail for user " + userId);
		
		return lemonReactiveService.resendVerificationMail(userId);
	}	


	/**
	 * Verifies current-user
	 */
	@PostMapping("/users/{id}/verification")
	public Mono<UserDto<ID>> verifyUser(
			@PathVariable ID id,
			@RequestParam String code,
			ServerHttpResponse response) {
		
		log.debug("Verifying user ...");		
		return userWithToken(lemonReactiveService.verifyUser(id, code), response);
	}

	
	/**
	 * The forgot Password feature
	 */
	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> forgotPassword(@RequestParam String email) {
		
		log.debug("Received forgot password request for: " + email);				
		return lemonReactiveService.forgotPassword(email);
	}

	
	/**
	 * Resets password after it's forgotten
	 */
	@PostMapping("/reset-password")
	public Mono<UserDto<ID>> resetPassword(
			@RequestParam @Valid @NotBlank String code,
		    @RequestParam @Valid @NotBlank String newPassword,
		    ServerHttpResponse response) {
		
		log.debug("Resetting password ... ");				
		return userWithToken(lemonReactiveService.resetPassword(code, newPassword), response);
	}

	
	/**
	 * Fetches a user by email
	 */
	@PostMapping("/users/fetch-by-email")
	public Mono<U> fetchUserByEmail(@RequestParam String email) {
		
		log.debug("Fetching user by email: " + email);						
		return lemonReactiveService.fetchUserByEmail(email);
	}

	
	/**
	 * Fetches a user by ID
	 */	
	@GetMapping("/users/{id}")
	public Mono<U> fetchUserById(@PathVariable ID id) {
		
		log.debug("Fetching user: " + id);				
		return lemonReactiveService.fetchUserById(id);
	}


	/**
	 * Updates a user
	 */
	@PatchMapping("/users/{id}")
	public Mono<UserDto<ID>> updateUser(
			@PathVariable ID id,
			@RequestBody Mono<String> patch,
			ServerHttpResponse response) {
		
		log.debug("Updating user ... ");
		return userWithToken(lemonReactiveService.updateUser(id, patch), response);
	}

	
	/**
	 * returns the current user and a new authorization token in the response
	 */
	protected Mono<UserDto<ID>> userWithToken(Mono<UserDto<ID>> userDto,
			ServerHttpResponse response) {
		
		return lemonReactiveService.userWithToken(userDto, response, jwtExpirationMillis);
	}
}
