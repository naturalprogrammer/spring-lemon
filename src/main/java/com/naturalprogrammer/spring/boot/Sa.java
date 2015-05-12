package com.naturalprogrammer.spring.boot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.boot.security.UserData;
import com.naturalprogrammer.spring.boot.security.UserDetailsImpl;
import com.naturalprogrammer.spring.boot.user.BaseUser;

@Component
public class Sa {

	private static ApplicationContext applicationContext;
	
	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) {
		
		Sa.applicationContext = applicationContext;
		
	}
	
	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
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
	
	public static BaseUser getSessionUser() {
	  UserDetailsImpl auth = getPrincipal();
	  return auth == null ? null : auth.getUser(); 
	}
	
	public static UserData getUserData() {
		BaseUser user = Sa.getSessionUser();
		if (user == null)
			return null;
		return user.getUserData();
	}

}
