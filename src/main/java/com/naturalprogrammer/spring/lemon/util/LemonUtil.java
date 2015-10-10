package com.naturalprogrammer.spring.lemon.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.VersionedEntity;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.exceptions.VersionException;

/**
 * Useful static methods
 * 
 * @author Sanjay Patel
 *
 */
@Component
public class LemonUtil {

	private static ApplicationContext applicationContext;
	private static MessageSource messageSource;
	
	@Autowired
	public LemonUtil(ApplicationContext applicationContext,
		MessageSource messageSource) {
		
		LemonUtil.applicationContext = applicationContext;
		LemonUtil.messageSource = messageSource;		
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
	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	U getUser() {
		
		// get the authentication object
		Authentication auth = SecurityContextHolder
			.getContext().getAuthentication();
		
		// get the user from the authentication object
		return getUser(auth);
	}
	

	/**
	 * Extracts the current-user from authentication object
	 * 
	 * @param auth
	 * @return
	 */
	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	U getUser(Authentication auth) {
		
	    if (auth != null) {
	      Object principal = auth.getPrincipal();
	      if (principal instanceof AbstractUser<?,?>) {
	        return (U) principal;
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
	void logIn(U user) {
		
	    user.decorate(user); // decorate self
		Authentication authentication = // make the authentication object
	    	new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
	    SecurityContextHolder.getContext().setAuthentication(authentication); // put that in the security context
	}
	

	/**
	 * Signs a user out
	 */
	public static void logOut() {
		SecurityContextHolder.getContext().setAuthentication(null); // set the authentication to null
	}


	/**
	 * Throws a VersionException if the versions of the
	 * given entities aren't same.
	 * 
	 * @param original
	 * @param updated
	 */
	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	void validateVersion(VersionedEntity<U,ID> original, VersionedEntity<U,ID> updated) {
		
		if (original.getVersion() != updated.getVersion())
			throw new VersionException(original.getClass().getSimpleName());
	}

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static MultiErrorException check(
			boolean valid, String messageKey, Object... args) {
		
		return LemonUtil.check(null, valid, messageKey, args);
	}

	
	/**
	 * Creates a MultiErrorException out of the given parameters
	 * 
	 * @param fieldName		the name of the field related to the error
	 * @param valid			the condition to check for
	 * @param messageKey	key of the error message
	 * @param args			any message arguments
	 */
	public static MultiErrorException check(
			String fieldName, boolean valid, String messageKey, Object... args) {
		
		return new MultiErrorException().check(fieldName, valid, messageKey, args);
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

}
