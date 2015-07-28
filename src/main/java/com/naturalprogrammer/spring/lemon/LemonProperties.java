package com.naturalprogrammer.spring.lemon;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties those are passed to client
 * 
 * @author Sanjay Patel
 *
 */
@Component
@ConfigurationProperties(prefix="lemon")
public class LemonProperties {
	
    /**
     * Properties which are passed to client.
     * Do not put secret properties inside this.
     */
	public static class Recaptcha {
    	
        /**
         * Set this false if you don't want to
         * enable ReCaptcha. Useful for
         * automated testing.
         */
		private boolean enabled = true;
		
		/**
         * Google ReCaptcha Site Key
         */
    	private String sitekey;
    	        
        /**
         * Google ReCaptcha Secret Key
         */
    	private String secretkey;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

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
	
	private Recaptcha recaptcha = new Recaptcha();
	private Admin admin = new Admin();
	
    public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	public Recaptcha getRecaptcha() {
		return recaptcha;
	}

	public void setRecaptcha(Recaptcha recaptcha) {
		this.recaptcha = recaptcha;
	}

	/**
     * Any shared properties you want to pass to the 
     * client should begin with lemon.shared.
     */
	private Map<String, Object> shared;

    
    
    public Map<String, Object> getShared() {
		return shared;
	}

	public void setShared(Map<String, Object> shared) {
		this.shared = shared;
	}



	/**
     * Secret string used for encrypting remember-me cookie
     */
    private String rememberMeKey;
    
	public String getRememberMeKey() {
		return rememberMeKey;
	}

	public void setRememberMeKey(String rememberMeKey) {
		this.rememberMeKey = rememberMeKey;
	}
    
    /**
	 * The URL users visit to use this deployment
	 */
    private String applicationUrl = "http://localhost:9000";
    
	public String getApplicationUrl() {
		return applicationUrl;
	}

	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}

}
