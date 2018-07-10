package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;

import reactor.core.publisher.Mono;

@Validated
public class LemonReactiveService
	<U extends AbstractMongoUser<ID>, ID extends Serializable> {

    private static final Log log = LogFactory.getLog(LemonReactiveService.class);
    
	private PasswordEncoder passwordEncoder;
	private AbstractMongoUserRepository<U, ID> userRepository;
	private JwtService jwtService;

	@Autowired
	public void createLemonService(
			PasswordEncoder passwordEncoder,
			AbstractMongoUserRepository<U, ID> userRepository,
			JwtService jwtService) {
		
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		
		log.info("Created");
	}

	/**
	 * Signs up a user.
	 */
	@Validated(UserUtils.SignUpValidation.class)
	public Mono<U> signup(@Valid Mono<U> user) {
		
		log.debug("Signing up user: " + user);
		
		return user
//			.onErrorResume(Mono::error)
			.doOnNext(this::encryptPassword)
			.flatMap(userRepository::insert);
	}
	
	private void encryptPassword(U user) {
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
	}

	public void addAuthHeader(ServerHttpResponse response, String username, long expirationMillis) {
		response.getHeaders().add(LecUtils.TOKEN_RESPONSE_HEADER_NAME, LecUtils.TOKEN_PREFIX +
				jwtService.createToken(JwtService.AUTH_AUDIENCE, username, expirationMillis));
	}
}
