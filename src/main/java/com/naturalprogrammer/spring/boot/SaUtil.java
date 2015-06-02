package com.naturalprogrammer.spring.boot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.boot.security.UserDto;
import com.naturalprogrammer.spring.boot.security.UserDetailsImpl;

@Component
public class SaUtil {

	public static final String APPLICATION_URL = "${application.url: http://localhost:9000}";
	public static final String RECAPTCHA_SITE_KEY = "${reCaptcha.siteKey: ReCaptcha Site Key Not Provided}";
	public static final String RECAPTCHA_SECRET_KEY = "${reCaptcha.secretKey: ReCaptcha Secret Key Not Provided}";
	
	private static ApplicationContext applicationContext;
	private static MessageSource messageSource;
	
	@Autowired
	public SaUtil(ApplicationContext applicationContext, MessageSource messageSource) {
		
		SaUtil.applicationContext = applicationContext;
		SaUtil.messageSource = messageSource;
		
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
	
	public static <U extends BaseUser<U,ID>, ID extends Serializable> UserDetailsImpl<U,ID> getPrincipal() {
		
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	
	    if (auth != null) {
	      Object principal = auth.getPrincipal();
	      if (principal instanceof UserDetailsImpl) {
	        return (UserDetailsImpl<U,ID>) principal;
	      }
	    }
	    return null;	  
	}
	
	public static <U extends BaseUser<U,ID>, ID extends Serializable> U getSessionUser() {
	  UserDetailsImpl<U,ID> auth = getPrincipal();
	  return auth == null ? null : auth.getUser(); 
	}
	
	public static <U extends BaseUser<U,ID>, ID extends Serializable> UserDto<ID> getUserDto() {
		BaseUser<U,ID> baseUser = SaUtil.getSessionUser();
		if (baseUser == null)
			return null;
		return baseUser.getUserDto();
	}
	
    public static <U extends BaseUser<U,ID>, ID extends Serializable> void logInUser(U user) {
    	
        UserDetailsImpl<U,ID> userDetails = new UserDetailsImpl<U,ID>(user);
 
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
