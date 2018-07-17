package com.naturalprogrammer.spring.lemonreactive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commons.LemonProperties.Admin;
import com.naturalprogrammer.spring.lemon.commons.domain.ChangePasswordForm;
import com.naturalprogrammer.spring.lemon.commons.mail.LemonMailData;
import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemon.commons.security.JwtService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import com.naturalprogrammer.spring.lemonreactive.forms.EmailForm;
import com.naturalprogrammer.spring.lemonreactive.util.LerUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

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
			MailSender mailSender,
			AbstractMongoUserRepository<U, ID> userRepository,
			ReactiveUserDetailsService userDetailsService,
			JwtService jwtService) {
		
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
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

		Mono<Optional<UserDto<ID>>> userDtoMono = LerUtils.currentUser();
		return userDtoMono.map(optionalUser -> {
			
			Map<String, Object> context = buildContext();
			optionalUser.ifPresent(user -> {
				addAuthHeader(response, user,
					expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
				context.put("user", user);
			});
			
			return context;			
		});
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
	public Mono<UserDto<ID>> signup(Mono<U> user) {
		
		log.debug("Signing up user: " + user);
		
		return user
			.doOnNext(this::initUser)
			.flatMap(userRepository::insert)
			.doOnSuccess(this::sendVerificationMail)
			.map(AbstractMongoUser::toUserDto);
	}

	
	protected void initUser(U user) {
		
		log.debug("Initializing user: " + user);

		user.setPassword(passwordEncoder.encode(user.getPassword())); // encode the password
		makeUnverified(user); // make the user unverified
	}	

	
	/**
	 * Makes a user unverified
	 */
	protected void makeUnverified(U user) {
		
		user.getRoles().add(UserUtils.Role.UNVERIFIED);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
	}
	
	
	/**
	 * Sends verification mail to a unverified user.
	 */
	protected void sendVerificationMail(final U user) {
		try {
			
			log.debug("Sending verification mail to: " + user);
			
			String verificationCode = jwtService.createToken(JwtService.VERIFY_AUDIENCE,
					user.getId().toString(), properties.getJwt().getExpirationMillis(),
					LecUtils.mapOf("email", user.getEmail()));

			// make the link
			String verifyLink = properties.getApplicationUrl()
				+ "/users/" + user.getId() + "/verification?code=" + verificationCode;

			// send the mail
			sendVerificationMail(user, verifyLink);

			log.debug("Verification mail to " + user.getEmail() + " queued.");
			
		} catch (Throwable e) {
			// In case of exception, just log the error and keep silent
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}	

	
	/**
	 * Sends verification mail to a unverified user.
	 * Override this method if you're using a different MailData
	 */
	protected void sendVerificationMail(final U user, String verifyLink) {
		
		// send the mail
		mailSender.send(LemonMailData.of(user.getEmail(),
			LexUtils.getMessage("com.naturalprogrammer.spring.verifySubject"),
			LexUtils.getMessage(
				"com.naturalprogrammer.spring.verifyEmail",	verifyLink)));
	}	

	
	/**
	 * Resends verification mail to the user.
	 */
	public Mono<Void> resendVerificationMail(ID userId) {
		
		return findUserById(userId)
			.zipWith(LerUtils.currentUser())
			.doOnNext(this::ensureEditable)
			.map(Tuple2::getT1)
			.doOnNext(this::resendVerificationMail).then();
	}

	
	protected void ensureEditable(Tuple2<U, Optional<UserDto<Serializable>>> tuple) {
		LecUtils.ensureAuthority(
				tuple.getT1().hasPermission(tuple.getT2().orElse(null),
				UserUtils.Permission.EDIT),
				"com.naturalprogrammer.spring.notGoodAdminOrSameUser");
	}

	
	/**
	 * Resends verification mail to the user.
	 */
	protected void resendVerificationMail(U user) {

		// must be unverified
		LexUtils.validate(user.getRoles().contains(UserUtils.Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	

		// send the verification mail
		sendVerificationMail(user);
	}


	public Mono<UserDto<ID>> verifyUser(ID userId, String code) {
		
		log.debug("Verifying user ...");

		return findUserById(userId)
				.doOnNext(user -> this.verifyUser(user, code))
				.flatMap(userRepository::save)
				.map(AbstractMongoUser::toUserDto);
	}

	
	public void verifyUser(U user, String verificationCode) {
		
		log.debug("Verifying user ...");

		// ensure that he is unverified
		LexUtils.validate(user.hasRole(UserUtils.Role.UNVERIFIED),
				"com.naturalprogrammer.spring.alreadyVerified").go();	
		
		JWTClaimsSet claims = jwtService.parseToken(verificationCode, JwtService.VERIFY_AUDIENCE, user.getCredentialsUpdatedMillis());
		
		LecUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("email").equals(user.getEmail()),
				"com.naturalprogrammer.spring.wrong.verificationCode");
		
		user.getRoles().remove(UserUtils.Role.UNVERIFIED); // make him verified
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
	}

	
	public Mono<Void> forgotPassword(String email) {
		
		return findUserByEmail(email)
				.doOnSuccess(this::mailForgotPasswordLink)
				.then();
	}

	
	/**
	 * Mails the forgot password link.
	 * 
	 * @param user
	 */
	public void mailForgotPasswordLink(U user) {
		
		log.debug("Mailing forgot password link to user: " + user);

		String forgotPasswordCode = jwtService.createToken(JwtService.FORGOT_PASSWORD_AUDIENCE,
				user.getEmail(), properties.getJwt().getExpirationMillis());

		// make the link
		String forgotPasswordLink =	properties.getApplicationUrl()
			    + "/reset-password?code=" + forgotPasswordCode;
		
		mailForgotPasswordLink(user, forgotPasswordLink);
		
		log.debug("Forgot password link mail queued.");
	}

	
	/**
	 * Mails the forgot password link.
	 * 
	 * Override this method if you're using a different MailData
	 */
	public void mailForgotPasswordLink(U user, String forgotPasswordLink) {
		
		// send the mail
		mailSender.send(LemonMailData.of(user.getEmail(),
				LexUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordSubject"),
				LexUtils.getMessage("com.naturalprogrammer.spring.forgotPasswordEmail",
					forgotPasswordLink)));
	}

		
	public Mono<UserDto<ID>> resetPassword(String forgotPasswordCode, String newPassword) {
		
		log.debug("Resetting password ...");

		JWTClaimsSet claims = jwtService.parseToken(forgotPasswordCode,
				JwtService.FORGOT_PASSWORD_AUDIENCE);
		
		String email = claims.getSubject();
		
		// fetch the user
		return findUserByEmail(email)
				.doOnNext(user -> resetPassword(user, claims, newPassword))
				.flatMap(userRepository::save)
				.map(AbstractMongoUser::toUserDto);
	}
	
	
	public void resetPassword(U user, JWTClaimsSet claims, String newPassword) {
		
		log.debug("Resetting password ...");

		LerUtils.ensureCredentialsUpToDate(claims, user);
		
		// sets the password
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		//user.setForgotPasswordCode(null);
		
		log.debug("Password reset.");		
	}

	/**
	 * returns the current user and a new authorization token in the response
	 */
	public Mono<UserDto<ID>> userWithToken(Mono<UserDto<ID>> userDto,
			ServerHttpResponse response, long expirationMillis) {

		return userDto.doOnNext(user -> {
			log.debug("Adding auth header for " + user.getUsername());
			addAuthHeader(response, user, expirationMillis);
		});
	}

	
	protected void addAuthHeader(ServerHttpResponse response, UserDto<ID> userDto, long expirationMillis) {
		
		log.debug("Adding auth header for " + userDto.getUsername());
		
		response.getHeaders().add(LecUtils.TOKEN_RESPONSE_HEADER_NAME, LecUtils.TOKEN_PREFIX +
			jwtService.createToken(JwtService.AUTH_AUDIENCE, userDto.getUsername(), expirationMillis));
	}


	public Mono<U> fetchUserByEmail(String email) {
		
		// fetch the user
		return findUserByEmail(email)
				.zipWith(LerUtils.currentUser())
				.doOnNext(this::hideConfidentialFields)
				.map(Tuple2::getT1);
	}


	public Mono<U> fetchUserById(ID userId) {
		// fetch the user
		return findUserById(userId)
				.zipWith(LerUtils.currentUser())
				.doOnNext(this::hideConfidentialFields)
				.map(Tuple2::getT1);
	}

	
	public Mono<UserDto<ID>> updateUser(ID userId, Mono<String> patch) {
		
		return Mono.zip(findUserById(userId), LerUtils.currentUser(), patch)
			.doOnNext(this::ensureEditable)
			.map((Tuple3<U, Optional<UserDto<Serializable>>, String> tuple3) ->
				this.updateUser(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()))
			.flatMap(userRepository::save)
			.map(user -> {
				UserDto<ID> userDto = user.toUserDto();
				userDto.setPassword(null);
				return userDto;
			});
	}


	protected U updateUser(U user, Optional<UserDto<Serializable>> currentUser, String patch) {
		
		log.debug("Updating user: " + user);

		U updatedUser = LerUtils.applyPatch(user, patch); // create a patched form
		LexUtils.validate("updatedUser", updatedUser, UserUtils.UpdateValidation.class);
		LerUtils.ensureCorrectVersion(user, updatedUser);
		
		updateUserFields(user, updatedUser, (UserDto<ID>) currentUser.get());

		log.debug("Updated user: " + user);
		return user;
	}
	
	
	/**
	 * Updates the fields of the users. Override this if you have more fields.
	 */
	protected void updateUserFields(U user, U updatedUser, UserDto<ID> currentUser) {

		log.debug("Updating user fields for user: " + user);

		// Another good admin must be logged in to edit roles
		if (currentUser.isGoodAdmin() &&
		   !currentUser.getId().equals(user.getId())) { 
			
			log.debug("Updating roles for user: " + user);

			// update the roles
			
			if (user.getRoles().equals(updatedUser.getRoles())) // roles are same
				return;
			
			if (updatedUser.hasRole(UserUtils.Role.UNVERIFIED)) {
				
				if (!user.hasRole(UserUtils.Role.UNVERIFIED)) {

					makeUnverified(user); // make user unverified
				}
			} else {
				
				if (user.hasRole(UserUtils.Role.UNVERIFIED))
					user.getRoles().remove(UserUtils.Role.UNVERIFIED); // make user verified
			}
			
			user.setRoles(updatedUser.getRoles());
			user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		}
	}

	
	public Mono<U> findUserByEmail(String email) {
		return userRepository
				.findByEmail(email)
				.switchIfEmpty(LerUtils.notFoundMono());
	}
	

	public Mono<U> findUserById(ID id) {
		return userRepository
				.findById(id)
				.switchIfEmpty(LerUtils.notFoundMono());
	}

	
	/**
	 * Hides the confidential fields before sending to client
	 */
	protected void hideConfidentialFields(Tuple2<U,Optional<UserDto<Serializable>>> tuple) {
		
		U user = tuple.getT1();
		
		user.setPassword(null); // JsonIgnore didn't work
		
		if (!user.hasPermission(tuple.getT2().orElse(null), UserUtils.Permission.EDIT))
			user.setEmail(null);
		
		log.debug("Hid confidential fields for user: " + user);
	}


	public Mono<UserDto<ID>> changePassword(ID userId, Mono<ChangePasswordForm> changePasswordForm) {
		
		return Mono.zip(findUserById(userId), LerUtils.currentUser())
				.doOnNext(this::ensureEditable)
				.flatMap(tuple -> Mono.zip(
						Mono.just(tuple.getT1()),
						findUserById(((UserDto<ID>)tuple.getT2().get()).getId()),
						changePasswordForm)
				.doOnNext(this::changePassword))
				.map(Tuple2::getT1)
				.flatMap(userRepository::save)
				.map(AbstractMongoUser::toUserDto);
	}
	
	protected void changePassword(Tuple3<U,U,ChangePasswordForm> tuple) {
		
		U user = tuple.getT1();
		U loggedIn = tuple.getT2();
		ChangePasswordForm changePasswordForm = tuple.getT3();
		
		log.debug("Changing password for user: " + user);
		
		String oldPassword = loggedIn.getPassword();

		LexUtils.validate("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
					oldPassword),
			"com.naturalprogrammer.spring.wrong.password").go();
		
		// sets the password
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		log.debug("Changed password for user: " + user);
	}


	public Mono<Void> requestEmailChange(ID userId, Mono<EmailForm> emailForm) {
		
		return Mono.zip(findUserById(userId), LerUtils.currentUser())
				.doOnNext(this::ensureEditable)
				.flatMap(tuple -> Mono.zip(
						Mono.just(tuple.getT1()),
						findUserById(((UserDto<ID>)tuple.getT2().get()).getId()),
						emailForm)
				.doOnNext(this::requestEmailChange))
				.map(Tuple2::getT1)
				.flatMap(userRepository::save)
				.doOnNext(this::mailChangeEmailLink)
				.then();
	}
	
	protected void requestEmailChange(Tuple3<U,U,EmailForm> tuple) {
		
		U user = tuple.getT1();
		U loggedIn = tuple.getT2();
		EmailForm emailForm = tuple.getT3();
		
		log.debug("Requesting email change: " + user);
		
		// checks
		LexUtils.validate("updatedUser.password",
			passwordEncoder.matches(emailForm.getPassword(),
									user.getPassword()),
			"com.naturalprogrammer.spring.wrong.password").go();

		// preserves the new email id
		user.setNewEmail(emailForm.getNewEmail());

		log.debug("Requested email change: " + user);		
	}
	
	
	/**
	 * Mails the change-email verification link to the user.
	 */
	protected void mailChangeEmailLink(U user) {
		
		String changeEmailCode = jwtService.createToken(JwtService.CHANGE_EMAIL_AUDIENCE,
				user.getId().toString(), properties.getJwt().getExpirationMillis(),
				LecUtils.mapOf("newEmail", user.getNewEmail()));
		
		try {
			
			log.debug("Mailing change email link to user: " + user);

			// make the link
			String changeEmailLink = properties.getApplicationUrl()
				    + "/users/" + user.getId()
					+ "/change-email?code=" + changeEmailCode;
			
			// mail it
			mailChangeEmailLink(user, changeEmailLink);
			
			log.debug("Change email link mail queued.");
			
		} catch (Throwable e) {
			// In case of exception, just log the error and keep silent			
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}
	
	
	/**
	 * Mails the change-email verification link to the user.
	 * 
	 * Override this method if you're using a different MailData
	 */
	protected void mailChangeEmailLink(U user, String changeEmailLink) {
		
		mailSender.send(LemonMailData.of(user.getNewEmail(),
				LexUtils.getMessage(
				"com.naturalprogrammer.spring.changeEmailSubject"),
				LexUtils.getMessage(
				"com.naturalprogrammer.spring.changeEmailEmail",
				 changeEmailLink)));
	}


	@PreAuthorize("isAuthenticated()")
	public Mono<UserDto<ID>> changeEmail(ID userId, Mono<MultiValueMap<String, String>> formData) {
		
		log.debug("Changing email of current user ...");
		
		return LerUtils.currentUser()
			.doOnNext(currentUser -> {				
				LexUtils.validate(userId.equals(currentUser.get().getId()),
						"com.naturalprogrammer.spring.wrong.login").go();	
			})
			.then(Mono.zip(findUserById(userId), formData))
			.map(this::validateChangeEmail)
			.flatMap(user -> Mono.zip(Mono.just(user),
					userRepository
						.findByEmail(user.getNewEmail())
						.map(Optional::of)
						.defaultIfEmpty(Optional.empty())
					))
			.map(this::changeEmail)
			.flatMap(userRepository::save)
			.map(AbstractMongoUser::toUserDto);
	}

	
	protected U validateChangeEmail(Tuple2<U, MultiValueMap<String, String>> tuple) {
		U user = tuple.getT1();
		String code = tuple.getT2().getFirst("code");
				
		LexUtils.validate(StringUtils.isNotBlank(code),
				"com.naturalprogrammer.spring.blank", "code").go();

		LexUtils.validate(StringUtils.isNotBlank(user.getNewEmail()),
				"com.naturalprogrammer.spring.blank.newEmail").go();
		
		JWTClaimsSet claims = jwtService.parseToken(code,
				JwtService.CHANGE_EMAIL_AUDIENCE,
				user.getCredentialsUpdatedMillis());
		
		LecUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("newEmail").equals(user.getNewEmail()),
				"com.naturalprogrammer.spring.wrong.changeEmailCode");
		
		return user;		
	}

	
	protected U changeEmail(Tuple2<U, Optional<U>> tuple) {
		
		U user = tuple.getT1();
		Optional<U> anotherUserWithSameEmail = tuple.getT2();
		
		// Ensure that the email would be unique 
		LexUtils.validate(!anotherUserWithSameEmail.isPresent(),
				"com.naturalprogrammer.spring.duplicate.email").go();	
		
		// update the fields
		user.setEmail(user.getNewEmail());
		user.setNewEmail(null);
		//user.setChangeEmailCode(null);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		
		// make the user verified if he is not
		if (user.hasRole(UserUtils.Role.UNVERIFIED))
			user.getRoles().remove(UserUtils.Role.UNVERIFIED);
		
		return user;
	}
}
