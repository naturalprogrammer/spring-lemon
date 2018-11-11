package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.forms.EmailForm;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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

    protected long jwtExpirationMillis;
	protected LemonReactiveService<U, ID> lemonReactiveService;	
	
	@Autowired
	public void createLemonController(
			LemonProperties properties,
			LemonReactiveService<U, ID> lemonReactiveService) {
		
		this.jwtExpirationMillis = properties.getJwt().getExpirationMillis();
		this.lemonReactiveService = lemonReactiveService;		

		log.info("Created");
	}

	
	/**
	 * Afgter a successful login, returns the current user with an authorization header.
	 */
	@PostMapping("/login")
	public Mono<UserDto> login(ServerWebExchange exchange) {
		
		log.debug("Returning current user ... ");
		
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getPrincipal)
				.cast(LemonPrincipal.class)
				.doOnNext(LemonPrincipal::eraseCredentials)
				.map(LemonPrincipal::currentUser)
				.zipWith(exchange.getFormData())
				.doOnNext(tuple -> {					
					long expirationMillis = lemonReactiveService.getExpirationMillis(tuple.getT2());
					lemonReactiveService.addAuthHeader(exchange.getResponse(), tuple.getT1(), expirationMillis);
				})
				.map(Tuple2::getT1);
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
	protected Mono<UserDto> signup(Mono<U> user, ServerHttpResponse response) {
		
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
	public Mono<UserDto> verifyUser(
			@PathVariable ID id,
			ServerWebExchange exchange) {
		
		log.debug("Verifying user ...");		
		return userWithToken(lemonReactiveService.verifyUser(id, exchange.getFormData()),
				exchange.getResponse());
	}

	
	/**
	 * The forgot Password feature
	 */
	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> forgotPassword(ServerWebExchange exchange) {
		
		log.debug("Received forgot password request ... " );				
		return lemonReactiveService.forgotPassword(exchange.getFormData());
	}

	
	/**
	 * Resets password after it's forgotten
	 */
	@PostMapping("/reset-password")
	public Mono<UserDto> resetPassword(
			@RequestBody @Valid Mono<ResetPasswordForm> form,
		    ServerHttpResponse response) {
		
		log.debug("Resetting password ... ");				
		return userWithToken(lemonReactiveService.resetPassword(form), response);
	}

	
	/**
	 * Fetches a user by email
	 */
	@PostMapping("/users/fetch-by-email")
	public Mono<U> fetchUserByEmail(ServerWebExchange exchange) {
		
		log.debug("Fetching user by email ... ");						
		return lemonReactiveService.fetchUserByEmail(exchange.getFormData());
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
	@PatchMapping(value = "/users/{id}")
	public Mono<UserDto> updateUser(
			@PathVariable ID id,
			@RequestBody @NotBlank Mono<String> patch,
			ServerHttpResponse response) {
		
		log.debug("Updating user ... ");
		return userWithToken(lemonReactiveService.updateUser(id, patch), response);
	}

	
	/**
	 * Changes password
	 */
	@PostMapping("/users/{id}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> changePassword(@PathVariable ID id,
			@RequestBody @Valid Mono<ChangePasswordForm> changePasswordForm,
			ServerHttpResponse response) {
		
		log.debug("Changing password ... ");
		return userWithToken(lemonReactiveService.changePassword(id, changePasswordForm), response).then();
	}


	/**
	 * Requests for changing email
	 */
	@PostMapping("/users/{id}/email-change-request")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> requestEmailChange(@PathVariable ID id,
			@RequestBody @Valid	Mono<EmailForm> emailForm) {
		
		log.debug("Requesting email change ... ");				
		return lemonReactiveService.requestEmailChange(id, emailForm);
	}	


	/**
	 * Changes the email
	 */
	@PostMapping("/users/{userId}/email")
	public Mono<UserDto> changeEmail(
			@PathVariable ID userId,
			ServerWebExchange exchange) {
		
		log.debug("Changing email of user ...");
		return userWithToken(lemonReactiveService.changeEmail(
				userId,
				exchange.getFormData()),
			exchange.getResponse());
	}

	
	/**
	 * Fetch a new token - for session sliding, switch user etc.
	 */
	@PostMapping("/fetch-new-auth-token")
	public Mono<Map<String, String>> fetchNewToken(ServerWebExchange exchange) {
		
		log.debug("Fetching a new token ... ");
		
		return lemonReactiveService.fetchNewToken(exchange);
		
		//return LecUtils.mapOf("token", lemonService.fetchNewToken(expirationMillis, username));
	}
	
	
	/**
	 * Fetch a self-sufficient token with embedded UserDto - for interservice communications
	 */
	@GetMapping("/fetch-full-token")
	public Mono<Map<String, String>> fetchFullToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		
		log.debug("Fetching a micro token");
		return lemonReactiveService.fetchFullToken(authHeader);
	}	

	
	/**
	 * returns the current user and a new authorization token in the response
	 */
	protected Mono<UserDto> userWithToken(Mono<UserDto> userDto,
			ServerHttpResponse response) {
		
		return lemonReactiveService.userWithToken(userDto, response, jwtExpirationMillis);
	}
}
