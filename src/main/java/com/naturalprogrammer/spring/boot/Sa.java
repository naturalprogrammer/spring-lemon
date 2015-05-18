package com.naturalprogrammer.spring.boot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.boot.entities.User;
import com.naturalprogrammer.spring.boot.security.UserData;
import com.naturalprogrammer.spring.boot.security.UserDetailsImpl;

@Component
public class Sa {

	public static final String APPLICATION_URL = "${application.url: http://localhost:9000}";
	
	private static ApplicationContext applicationContext;
	private static MessageSource messageSource;
	
	@Autowired
	public Sa(ApplicationContext applicationContext, MessageSource messageSource) {
		
		Sa.applicationContext = applicationContext;
		Sa.messageSource = messageSource;
		
	}

	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}
	
	public static String getMessage(String messageKey, Object... args) {
		
		// http://stackoverflow.com/questions/10792551/how-to-obtain-a-current-user-locale-from-spring-without-passing-it-as-a-paramete
		return messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
	}
	
	@SuppressWarnings("unchecked")
	public static <K,V> Map<K,V> mapOf(Object... keyValPair) {
		
	    Map<K,V> map = new HashMap<K,V>();

	    if(keyValPair.length % 2 != 0){
	        throw new IllegalArgumentException("Keys and values must be pairs.");
	    }

	    for(int i = 0; i < keyValPair.length; i += 2){
	        map.put((K) keyValPair[i], (V) keyValPair[i+1]);
	    }

	    return map;
	}
	
	public static UserDetailsImpl getPrincipal() {
		
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	
	    if (auth != null) {
	      Object principal = auth.getPrincipal();
	      if (principal instanceof UserDetailsImpl) {
	        return (UserDetailsImpl) principal;
	      }
	    }
	    return null;	  
	}
	
	public static User getSessionUser() {
	  UserDetailsImpl auth = getPrincipal();
	  return auth == null ? null : auth.getUser(); 
	}
	
	public static UserData getUserData() {
		User user = Sa.getSessionUser();
		if (user == null)
			return null;
		return user.getUserData();
	}

}
