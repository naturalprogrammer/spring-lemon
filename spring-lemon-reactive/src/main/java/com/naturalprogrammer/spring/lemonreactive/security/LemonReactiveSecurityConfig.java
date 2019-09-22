package com.naturalprogrammer.spring.lemonreactive.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsreactive.security.LemonCommonsReactiveSecurityConfig;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import reactor.core.publisher.Mono;

public class LemonReactiveSecurityConfig<U extends AbstractMongoUser<ID>, ID extends Serializable> extends LemonCommonsReactiveSecurityConfig {

	private static final Log log = LogFactory.getLog(LemonReactiveSecurityConfig.class);
	
	protected LemonReactiveUserDetailsService<U, ID> userDetailsService;
	private LemonProperties properties;
	private ReactiveOAuth2AuthenticationSuccessHandler reactiveOAuth2AuthenticationSuccessHandler;

	public LemonReactiveSecurityConfig(BlueTokenService blueTokenService,
			LemonReactiveUserDetailsService<U, ID> userDetailsService,
			ReactiveOAuth2AuthenticationSuccessHandler reactiveOAuth2AuthenticationSuccessHandler,
			LemonProperties properties) {
		
		super(blueTokenService);
		this.userDetailsService = userDetailsService;
		this.reactiveOAuth2AuthenticationSuccessHandler = reactiveOAuth2AuthenticationSuccessHandler;
		this.properties = properties;
		
		log.info("Created");
	}

	/**
	 * Configure form login
	 */
	@Override
	protected void formLogin(ServerHttpSecurity http) {
		
		http.formLogin()
			.loginPage(loginPage()) // Should be "/login" by default, but not providing that overwrites our AuthenticationFailureHandler, because this is called later 
			.authenticationFailureHandler(authenticationFailureHandler())
			.authenticationSuccessHandler(new WebFilterChainServerAuthenticationSuccessHandler());
	}

	/**
	 * Override this to change login URL
	 */
	protected String loginPage() {
		
		return "/api/core/login";
	}

	/**
	 * Configure OAuth2 login
	 */
	@Override
	protected void oauth2Login(ServerHttpSecurity http) {

		http.oauth2Login()
			.authorizedClientRepository(new ReactiveCookieServerOAuth2AuthorizedClientRepository(properties))
			.authenticationSuccessHandler(reactiveOAuth2AuthenticationSuccessHandler)
			.authenticationManager(authenticationManager);
	}
	
	@Override
	protected Mono<UserDto> fetchUserDto(JWTClaimsSet claims) {
		
		String username = claims.getSubject();
		
		return userDetailsService.findUserByUsername(username)
			.switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException(username))))
			.doOnNext(user -> {
		        log.debug("User found ...");
		        LerUtils.ensureCredentialsUpToDate(claims, user);
			})
			.map(AbstractMongoUser::toUserDto);
	}
}
