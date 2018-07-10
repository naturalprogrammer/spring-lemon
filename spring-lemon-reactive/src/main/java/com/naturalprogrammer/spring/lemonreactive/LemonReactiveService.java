package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties.Admin;
import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;

import reactor.core.publisher.Mono;

@Validated
public abstract class LemonReactiveService
	<U extends AbstractMongoUser<ID>, ID extends Serializable> {

    private static final Log log = LogFactory.getLog(LemonReactiveService.class);
    
	private LemonProperties properties;
	private PasswordEncoder passwordEncoder;
    private MailSender mailSender;
	private AbstractMongoUserRepository<U, ID> userRepository;
	private ReactiveUserDetailsService userDetailsService;
	private JwtService jwtService;

	@Autowired
	public void createLemonService(LemonProperties properties,
			PasswordEncoder passwordEncoder,
			AbstractMongoUserRepository<U, ID> userRepository,
			ReactiveUserDetailsService userDetailsService,
			JwtService jwtService) {
		
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
		
		log.info("Created");
	}


	/**
     * This method is called after the application is ready.
     * Needs to be public - otherwise Spring screams.
     * 
     * @param event
     */
    @EventListener
    public void afterApplicationReady(ApplicationReadyEvent event) {
    	
    	log.info("Starting up Spring Lemon ...");
    	onStartup(); // delegate to onStartup()
    	log.info("Spring Lemon started");	
    }

    
    public void onStartup() {
		
		userDetailsService
			.findByUsername(properties.getAdmin().getUsername()) // Check if the user already exists
			.doOnError(e -> e instanceof UsernameNotFoundException, e -> {
				// Doesn't exist. So, create it.
		    	U user = createAdminUser();
		    	userRepository.insert(user).subscribe();								
			}).subscribe();
	}

    
	/**
	 * Creates the initial Admin user.
	 * Override this if needed.
	 */
    protected U createAdminUser() {
		
    	// fetch data about the user to be created
    	Admin initialAdmin = properties.getAdmin();
    	
    	log.info("Creating the first admin user: " + initialAdmin.getUsername());

    	// create the user
    	U user = newUser();
    	user.setEmail(initialAdmin.getUsername());
		user.setPassword(passwordEncoder.encode(
			properties.getAdmin().getPassword()));
		user.getRoles().add(UserUtils.Role.ADMIN);
		
		return user;
	}

    
	/**
	 * Creates a new user object. Must be overridden in the
	 * subclass, like this:
	 * 
	 * <pre>
	 * public User newUser() {
	 *    return new User();
	 * }
	 * </pre>
	 */
    public abstract U newUser();

    
	/**
	 * Returns the context data to be sent to the client,
	 * i.e. <code>reCaptchaSiteKey</code> and all the properties
	 * prefixed with <code>lemon.shared</code>.
	 * 
	 * To send custom properties, put those in your application
	 * properties in the format <em>lemon.shared.fooBar</em>.
	 * 
	 * If a user is logged in, it also returns the user data
	 * and a new authorization token. If expirationMillis is not provided,
	 * the expiration of the new token is set to the default.
	 *
	 * Override this method if needed.
	 */
	public Mono<Map<String, Object>> getContext(Optional<Long> expirationMillis, ServerHttpResponse response) {
		
		log.debug("Getting context ...");

		return LerUtils.currentUser().map(currentUser -> {
			
			addAuthHeader(response, currentUser.getUsername(),
				expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
			
			Map<String, Object> context = buildContext();
			
			context.put("user", currentUser);
			return context;
			
		}).switchIfEmpty(Mono.fromCallable(this::buildContext));
	}


	protected Map<String, Object> buildContext() {
		
		// make the context
		Map<String, Object> sharedProperties = new HashMap<String, Object>(2);
		sharedProperties.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
		sharedProperties.put("shared", properties.getShared());
		
		Map<String, Object> context = new HashMap<>();
		context.put("context", sharedProperties);
		
		return context;		
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
