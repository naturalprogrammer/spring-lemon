package com.naturalprogrammer.spring.lemonreactive.security;

import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

import java.io.Serializable;

public class LemonReactiveUserDetailsService<U extends AbstractMongoUser<ID>, ID extends Serializable>
		implements ReactiveUserDetailsService {

	private static final Log log = LogFactory.getLog(LemonReactiveUserDetailsService.class);

	private final AbstractMongoUserRepository<U, ID> userRepository;

	public LemonReactiveUserDetailsService(AbstractMongoUserRepository<U, ID> userRepository) {

		this.userRepository = userRepository;
		log.info("Created");
	}

	@Override
	public Mono<UserDetails> findByUsername(String username) {

		log.debug("Loading user having username: " + username);

		// delegates to findUserByUsername
		return findUserByUsername(username).switchIfEmpty(Mono.defer(() -> {
			log.debug("Could not find user " + username);
			return Mono.error(new UsernameNotFoundException(
				LexUtils.getMessage("com.naturalprogrammer.spring.userNotFound", username)));
		})).map(U::toUserDto).map(LemonPrincipal::new);
	}

	/**
	 * Finds a user by the given username. Override this if you aren't using email
	 * as the username.
	 */
	public Mono<U> findUserByUsername(String username) {
		return userRepository.findByEmail(username);
	}
}
