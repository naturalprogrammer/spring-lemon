package com.naturalprogrammer.spring.lemonreactive.security;

import java.io.Serializable;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemon.commons.security.JwtAuthenticationToken;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class LemonReactiveSecurityConfig <U extends AbstractMongoUser<ID>, ID extends Serializable> {

	private static final Log log = LogFactory.getLog(LemonReactiveSecurityConfig.class);
	
	private JwtService jwtService;
	private LemonReactiveUserDetailsService<U, ID> userDetailsService;

	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		
		log.info("Configuring SecurityWebFilterChain ...");
		
		return http
			.authorizeExchange()
				.anyExchange().permitAll()
			.and()
				.formLogin()
					.loginPage("/api/core/login") // Should be "/login" by default, but not providing that overwrites our AuthenticationFailureHandler, because this is called later 
					.authenticationFailureHandler(authenticationFailureHandler())
					.authenticationSuccessHandler(new WebFilterChainServerAuthenticationSuccessHandler())
			.and()
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
			.exceptionHandling()
				.accessDeniedHandler(accessDeniedHandler())
				.authenticationEntryPoint(authenticationEntryPoint())
			.and()
				.csrf().disable()
				.addFilterAt(tokenAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)				
			.build();
	}
	
	protected AuthenticationWebFilter tokenAuthenticationFilter() {
		
		AuthenticationWebFilter filter = new AuthenticationWebFilter(tokenAuthenticationManager());		
		filter.setAuthenticationConverter(tokenAuthenticationConverter());
		filter.setAuthenticationFailureHandler(authenticationFailureHandler());
		
		return filter;
	}
	
	protected ReactiveAuthenticationManager tokenAuthenticationManager() {
		
		return authentication -> {
			
			log.debug("Authenticating with token ...");

			String token = (String) authentication.getCredentials();
			
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
					.doOnNext(LemonPrincipal<ID>::eraseCredentials)
					.map(principal -> new JwtAuthenticationToken(principal, token, principal.getAuthorities()));		
		};
	}

	protected Function<ServerWebExchange, Mono<Authentication>> tokenAuthenticationConverter() {
		
		return serverWebExchange -> {
			
			String authorization = serverWebExchange.getRequest()
				.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			
			if(authorization == null || !authorization.startsWith(LecUtils.TOKEN_PREFIX))
				return Mono.empty();

			return Mono.just(new JwtAuthenticationToken(authorization.substring(7)));		
		};
	}
	
	protected ServerAuthenticationFailureHandler authenticationFailureHandler() {
		
		return (webFilterExchange, exception) -> Mono.error(exception);		
	}
	
	protected ServerAccessDeniedHandler accessDeniedHandler() {
		
		return (webFilterExchange, exception) -> Mono.error(exception);
	}
	
	protected ServerAuthenticationEntryPoint authenticationEntryPoint() {
		
		return (webFilterExchange, exception) -> Mono.error(exception);
	}
	
}
