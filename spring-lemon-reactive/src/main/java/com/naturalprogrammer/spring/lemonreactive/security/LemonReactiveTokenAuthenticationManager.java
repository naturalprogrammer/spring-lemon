package com.naturalprogrammer.spring.lemonreactive.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.naturalprogrammer.spring.lemon.commons.security.JwtAuthenticationToken;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import reactor.core.publisher.Mono;

public class LemonReactiveTokenAuthenticationManager
	<U extends AbstractMongoUser<ID>, ID extends Serializable>
	implements ReactiveAuthenticationManager {

    private static final Log log = LogFactory.getLog(LemonReactiveTokenAuthenticationManager.class);

	private final JwtService jwtService;
	private LemonReactiveUserDetailsService<U, ID> userDetailsService;
	
	public LemonReactiveTokenAuthenticationManager(JwtService jwtService,
			LemonReactiveUserDetailsService<U, ID> userDetailsService) {

		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		
		log.debug("Created");
	}

	@Override
	public Mono<Authentication> authenticate(Authentication auth) {
		
		log.debug("Authenticating ...");

		String token = (String) auth.getCredentials();
		
		JWTClaimsSet claims = jwtService.parseToken(token, JwtService.AUTH_AUDIENCE);
		
        String username = claims.getSubject();
        
		return userDetailsService.findUserByUsername(username)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException(username))))
				.doOnNext(user -> {
			        log.debug("User found ...");
			        LerUtils.ensureCredentialsUpToDate(claims, user);
				})
				.map(AbstractMongoUser::toUserDto)
				.map(LemonPrincipal<ID>::new)
				.map(principal -> new JwtAuthenticationToken(principal, token, principal.getAuthorities()));
	}
}
