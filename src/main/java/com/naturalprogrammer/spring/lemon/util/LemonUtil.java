package com.naturalprogrammer.spring.lemon.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.exception.ExceptionUtils;
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

	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}
	
	public static String getMessage(String messageKey, Object... args) {
		
		// http://stackoverflow.com/questions/10792551/how-to-obtain-a-current-user-locale-from-spring-without-passing-it-as-a-paramete
		return messageSource.getMessage(messageKey, args,
				LocaleContextHolder.getLocale());
	}
	
	@SuppressWarnings("unchecked")
	public static <K,V> Map<K,V> mapOf(Object... keyValPair) {
		
	    if(keyValPair.length % 2 != 0)
	        throw new IllegalArgumentException("Keys and values must be in pairs");
	
	    Map<K,V> map = new HashMap<K,V>();
	
	    for(int i = 0; i < keyValPair.length; i += 2){
	        map.put((K) keyValPair[i], (V) keyValPair[i+1]);
	    }
	
	    return map;
	}	

	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	U getUser() {
		
		Authentication auth = SecurityContextHolder
			.getContext().getAuthentication();
		
		return getUser(auth);
	}
	
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
	
	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	void logIn(U user) {
		
	    Authentication authentication =
	    	new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
	    SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	public static void logOut() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	public static <U extends AbstractUser<U,ID>, ID extends Serializable>
	void validateVersion(VersionedEntity<U,ID> original, VersionedEntity<U,ID> updated) {
		
		if (original.getVersion() != updated.getVersion())
			throw new VersionException(original.getClass().getSimpleName());
	}

	public static MultiErrorException check(
			boolean valid, String messageKey, Object... args) {
		return LemonUtil.check(null, valid, messageKey, args);
	}

	public static MultiErrorException check(
			String fieldName, boolean valid, String messageKey, Object... args) {
		return new MultiErrorException().check(fieldName, valid, messageKey, args);
	}

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
