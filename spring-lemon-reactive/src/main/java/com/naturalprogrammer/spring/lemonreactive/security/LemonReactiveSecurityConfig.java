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
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class LemonReactiveSecurityConfig <U extends AbstractMongoUser<ID>, ID extends Serializable> {

	private static final Log log = LogFactory.getLog(LemonReactiveSecurityConfig.class);
	
	protected JwtService jwtService;
	protected LemonReactiveUserDetailsService<U, ID> userDetailsService;

	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		
		log.info("Configuring SecurityWebFilterChain ...");
		
		return http
			.authorizeExchange()
				.anyExchange().permitAll()
			.and()
				.formLogin()
					.loginPage(loginPage()) // Should be "/login" by default, but not providing that overwrites our AuthenticationFailureHandler, because this is called later 
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
			.logout().disable()
			.build();
	}

	
	/**
	 * Override this to change login URL
	 * 
	 * @return
	 */
	protected String loginPage() {
		
		return "/api/core/login";
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
			
			UserDto userDto = getUserDto(claims);
			
			Mono<UserDto> userDtoMono;
			
			if (userDto == null) {
				
		        String username = claims.getSubject();

				userDtoMono = userDetailsService.findUserByUsername(username)
					.switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException(username))))
					.doOnNext(user -> {
				        log.debug("User found ...");
				        LerUtils.ensureCredentialsUpToDate(claims, user);
					})
					.map(AbstractMongoUser::toUserDto);
			} else
				userDtoMono = Mono.just(userDto);
			
			return userDtoMono.map(LemonPrincipal::new)
					.doOnNext(LemonPrincipal::eraseCredentials)
					.map(principal -> new JwtAuthenticationToken(principal, token, principal.getAuthorities()));		
		};
	}

	protected UserDto getUserDto(JWTClaimsSet claims) {

		return null;
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
		
		return (exchange, exception) -> Mono.error(exception);
	}
	
	protected ServerAuthenticationEntryPoint authenticationEntryPoint() {
		
		return (exchange, exception) -> Mono.error(exception);
	}
	
}
