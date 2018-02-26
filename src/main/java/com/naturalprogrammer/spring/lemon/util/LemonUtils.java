package com.naturalprogrammer.spring.lemon.util;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.VersionedEntity;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
import com.naturalprogrammer.spring.lemon.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.security.SpringUser;
import com.naturalprogrammer.spring.lemon.validation.FieldError;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Useful static methods
 * 
 * @author Sanjay Patel
 *
 */
@Component
public class LemonUtils {
	
	private static final Log log = LogFactory.getLog(LemonUtils.class);

	private static ApplicationContext applicationContext;
	private static MessageSource messageSource;
	private static ObjectMapper objectMapper;
	public static final MultiErrorException NOT_FOUND_EXCEPTION = new MultiErrorException();
	
	public LemonUtils(ApplicationContext applicationContext,
		MessageSource messageSource,
		ObjectMapper objectMapper) {
		
		LemonUtils.applicationContext = applicationContext;
		LemonUtils.messageSource = messageSource;
		LemonUtils.objectMapper = objectMapper;
		
		log.info("Created");
	}
	
	@PostConstruct
	public void postConstruct() {
		
		NOT_FOUND_EXCEPTION.getErrors().add(
				new FieldError(null, "com.naturalprogrammer.spring.notFound",
				getMessage("com.naturalprogrammer.spring.notFound")));
		
		NOT_FOUND_EXCEPTION.httpStatus(HttpStatus.NOT_FOUND);
		
		log.info("NOT_FOUND_EXCEPTION built");		
	}


	public static ObjectMapper getMapper() {
		
		return objectMapper;
	}
	
	/**
	 * Gets the reference to an application-context bean
	 *  
	 * @param clazz	the type of the bean
	 */
	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}
	
	
	/**
	 * Gets a message from messages.properties
	 * 
	 * @param messageKey	the key of the message
	 * @param args			any arguments
	 */
	public static String getMessage(String messageKey, Object... args) {
		
		if (messageSource == null)
			return "ApplicationContext unavailable, probably unit going on";
		
		// http://stackoverflow.com/questions/10792551/how-to-obtain-a-current-user-locale-from-spring-without-passing-it-as-a-paramete
		return messageSource.getMessage(messageKey, args,
				LocaleContextHolder.getLocale());
	}
	

	/**
	 * Constructs a map of the key-value pairs,
	 * passed as parameters
	 * 
	 * @param keyValPair
	 */
	@SuppressWarnings("unchecked")
	public static <K,V> Map<K,V> mapOf(Object... keyValPair) {
		
	    if(keyValPair.length % 2 != 0)
	        throw new IllegalArgumentException("Keys and values must be in pairs");
	
	    Map<K,V> map = new HashMap<K,V>(keyValPair.length / 2);
	
	    for(int i = 0; i < keyValPair.length; i += 2){
	        map.put((K) keyValPair[i], (V) keyValPair[i+1]);
	    }
	
	    return map;
	}	

	
	/**
	 * Gets the current-user
	 */
	public static <ID extends Serializable> SpringUser<ID> getSpringUser() {
		
		// get the authentication object
		Authentication auth = SecurityContextHolder
			.getContext().getAuthentication();
		
		// get the user from the authentication object
		return getSpringUser(auth);
	}
	

	/**
	 * Extracts the current-user from authentication object
	 * 
	 * @param auth
	 * @return
	 */
	public static <ID extends Serializable> SpringUser<ID> getSpringUser(Authentication auth) {
		
	    if (auth != null) {
	      Object principal = auth.getPrincipal();
	      if (principal instanceof LemonPrincipal<?>) {
	        return ((LemonPrincipal<ID>) principal).getSpringUser();
	      }
	    }
	    return null;	  
	}
	
	
	/**
	 * Signs a user in
	 * 
	 * @param user
	 */
	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	void login(U user) {
		
		LemonPrincipal<ID> principal = new LemonPrincipal<>(user.toSpringUser());

		Authentication authentication = // make the authentication object
	    	new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

	    SecurityContextHolder.getContext().setAuthentication(authentication); // put that in the security context
	    principal.eraseCredentials();
	}
	
//
//	/**
//	 * Signs a user out
//	 */
//	public static void logOut() {
//		SecurityContextHolder.getContext().setAuthentication(null); // set the authentication to null
//	}


	/**
	 * Throws a VersionException if the versions of the
	 * given entities aren't same.
	 * 
	 * @param original
	 * @param updated
	 */
	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	void ensureCorrectVersion(VersionedEntity<U,ID> original, VersionedEntity<U,ID> updated) {
		
		if (original.getVersion() != updated.getVersion())
			throw new VersionException(original.getClass().getSimpleName());
	}
	
	public static void ensureCredentials(boolean valid, String messageKey) {
		
		if (!valid)
			throw new BadCredentialsException(getMessage(messageKey));
	}

	public static void ensureAuthority(boolean authorized, String messageKey) {
		
		if (!authorized)
			throw new AccessDeniedException(getMessage(messageKey));
	}
	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static MultiErrorException validate(
			boolean valid, String messageKey, Object... args) {
		
		return LemonUtils.validate(null, valid, messageKey, args);
	}

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param fieldName		the name of the field related to the error
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static MultiErrorException validate(
			String fieldName, boolean valid, String messageKey, Object... args) {
		
		return new MultiErrorException().validate(fieldName, valid, messageKey, args);
	}

	public static <T> void ensureFound(T entity) {
		
		LemonUtils.validate("id", entity != null,
				"com.naturalprogrammer.spring.notFound")
				.httpStatus(HttpStatus.NOT_FOUND).go();
	}
	
	public static Supplier<MultiErrorException> notFoundSupplier() {
		
		return () -> NOT_FOUND_EXCEPTION;
	}	
	
	/**
	 * A convenient method for running code
	 * after successful database commit.
	 *  
	 * @param runnable
	 */
	public static void afterCommit(Runnable runnable) {
		
		TransactionSynchronizationManager.registerSynchronization(
		    new TransactionSynchronizationAdapter() {
		        @Override
		        public void afterCommit() {
		        	
		        	runnable.run();
		        }
		});				
	}

	public static String uid() {
		
		return UUID.randomUUID().toString();
	}
	
	
    @SuppressWarnings("unchecked")
	public static <T> T applyPatch(T originalObj, String patchString)
			throws JsonProcessingException, IOException, JsonPatchException {

        // Parse the patch to JsonNode
        JsonNode patchNode = objectMapper.readTree(patchString);

        // Create the patch
        JsonPatch patch = JsonPatch.fromJson(patchNode);

        // Convert the original object to JsonNode
        JsonNode originalObjNode = objectMapper.valueToTree(originalObj);

        // Apply the patch
        TreeNode patchedObjNode = patch.apply(originalObjNode);

        // Convert the patched node to an updated obj
        return objectMapper.treeToValue(patchedObjNode, (Class<T>) originalObj.getClass());
    }
    
	public static <T> String toJson(T obj) throws JsonProcessingException {

		return objectMapper.writeValueAsString(obj);
	}
	
	public static <T> T fromJson(String json, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, clazz);
	}

	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	void ensureUpToDate(JWTClaimsSet claims, U user) {
		
		ensureCredentials(claims.getIssueTime().after(user.getCredentialsUpdatedAt()),
				"com.naturalprogrammer.spring.obsoleteToken");
	}
	
	public static String toString(Resource resource) throws IOException {
		
		String text = null;
	    try (Scanner scanner = new Scanner(resource.getInputStream(), StandardCharsets.UTF_8.name())) {
	        text = scanner.useDelimiter("\\A").next();
	    }
	    
	    return text;
	}
}
