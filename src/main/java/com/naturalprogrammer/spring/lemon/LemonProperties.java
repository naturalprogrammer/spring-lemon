package com.naturalprogrammer.spring.lemon;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.security.CsrfCookieFilter;

/**
 * Lemon Properties
 * 
 * @author Sanjay Patel
 *
 */
@Component
@ConfigurationProperties(prefix="lemon")
public class LemonProperties {
	
    /**
	 * The URL users visit to use this deployment
	 */
    private String applicationUrl = "http://localhost:9000";
    
	/**
     * Secret string used for encrypting remember-me cookie
     */
    private String rememberMeKey;
    
    /**
	 * Recaptcha related properties
	 */
	private Recaptcha recaptcha = new Recaptcha();
	
    /**
	 * CORS related properties
	 */
	private Cors cors = new Cors();

    /**
	 * Properties related to initial Admin to be created
	 */
	private Admin admin = new Admin();
	
	/**
     * Any shared properties you want to pass to the 
     * client should begin with lemon.shared.
     */
	private Map<String, Object> shared;



	/**************************
	 * Gettrer and setters
	 **************************/
	public Recaptcha getRecaptcha() {
		return recaptcha;
	}

	public void setRecaptcha(Recaptcha recaptcha) {
		this.recaptcha = recaptcha;
	}


	public Cors getCors() {
		return cors;
	}

	public void setCors(Cors cors) {
		this.cors = cors;
	}

    public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

    public Map<String, Object> getShared() {
		return shared;
	}

	public void setShared(Map<String, Object> shared) {
		this.shared = shared;
	}

	public String getRememberMeKey() {
		return rememberMeKey;
	}

	public void setRememberMeKey(String rememberMeKey) {
		this.rememberMeKey = rememberMeKey;
	}
    
	public String getApplicationUrl() {
		return applicationUrl;
	}

	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}

	
	/**************************
	 * Static classes
	 *************************/
	
    /**
     * Recaptcha related properties
     */
	public static class Recaptcha {
    	
//        /**
//         * Set this false if you don't want to
//         * enable ReCaptcha. E.g. while
//         * automated testing.
//         */
//		private boolean enabled = true;
//		
		/**
         * Google ReCaptcha Site Key
         */
    	private String sitekey;
    	        
        /**
         * Google ReCaptcha Secret Key
         */
    	private String secretkey;

//		public boolean isEnabled() {
//			return enabled;
//		}
//
//		public void setEnabled(boolean enabled) {
//			this.enabled = enabled;
//		}
//
		public String getSitekey() {
			return sitekey;
		}

		public void setSitekey(String sitekey) {
			this.sitekey = sitekey;
		}

		public String getSecretkey() {
			return secretkey;
		}

		public void setSecretkey(String secretkey) {
			this.secretkey = secretkey;
		}
    	
    }
	
	
    /**
     * CORS configuration related properties
     */
	public static class Cors {
		
		/**
		 * Comma separated whitelisted URLs for CORS
		 */
		private String[] allowedOrigins;
		
		/**
		 * Comma separated methods to be allowed
		 */
		private String[] allowedMethods = {"GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "OPTIONS", "PATCH"};
		
		/**
		 * Comma separated headers to be allowed
		 */
		private String[] allowedHeaders = {"x-requested-with", "origin", "content-type", "accept", CsrfCookieFilter.XSRF_TOKEN_HEADER_NAME};
		
		/**
		 * Comma separated response headers to be exposed to the client.
		 * 
		 * See http://stackoverflow.com/questions/25673089/why-is-access-control-expose-headers-needed#answer-25673446
		 * for why this could be needed.
		 */		
		private String[] exposedHeaders = {"x-requested-with", "origin", "content-type", "accept", CsrfCookieFilter.XSRF_TOKEN_HEADER_NAME};
		
		/**
		 * CORS maxAge long property
		 */
		private long maxAge = 3600L;

		public String[] getAllowedOrigins() {
			return allowedOrigins;
		}

		public void setAllowedOrigins(String[] allowedOrigins) {
			this.allowedOrigins = allowedOrigins;
		}

		public String[] getAllowedMethods() {
			return allowedMethods;
		}

		public void setAllowedMethods(String[] allowedMethods) {
			this.allowedMethods = allowedMethods;
		}

		public String[] getAllowedHeaders() {
			return allowedHeaders;
		}

		public void setAllowedHeaders(String[] allowedHeaders) {
			this.allowedHeaders = allowedHeaders;
		}

		public String[] getExposedHeaders() {
			return exposedHeaders;
		}

		public void setExposedHeaders(String[] exposedHeaders) {
			this.exposedHeaders = exposedHeaders;
		}

		public long getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(long maxAge) {
			this.maxAge = maxAge;
		}
		
    }

	
	/**
	 * Properties regarding the first Admin to be created
	 * 
	 * @author Sanjay Patel
	 *
	 */
	public static class Admin {
		
		/**
		 * Login ID of first admin user to be created 
		 */
		private String login;
		
		/**
		 * Password of first admin user to be created 
		 */		
		private String password;

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
	}
	
}
