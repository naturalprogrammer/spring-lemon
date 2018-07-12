package com.naturalprogrammer.spring.lemonreactive.security;

import java.util.function.Function;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.security.JwtAuthenticationToken;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;

import reactor.core.publisher.Mono;

/**
 * See ServerHttpBasicAuthenticationConverter for example
 * 
 * @author Sanjay
 *
 */
public class LemonReactiveTokenAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {

	@Override
	public Mono<Authentication> apply(ServerWebExchange exchange) {
		
		String authorization = exchange.getRequest()
			.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		
		if(authorization == null || !authorization.startsWith(LecUtils.TOKEN_PREFIX))
			return Mono.empty();

		return Mono.just(new JwtAuthenticationToken(authorization.substring(7)));		
	}

}
