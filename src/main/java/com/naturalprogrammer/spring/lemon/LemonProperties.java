package com.naturalprogrammer.spring.lemon;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.validation.annotation.Validated;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

/**
 * Lemon Properties
 * 
 * @author Sanjay Patel
 *
 */
@Validated
@ConfigurationProperties(prefix="lemon")
public class LemonProperties {
	
    private static final Log log = LogFactory.getLog(LemonProperties.class);
    
    public LemonProperties() {
		log.info("Created");
	}

	/**
	 * Client web application's base URL.
	 * Used in the verification link mailed to the users, etc.
	 */
    private String applicationUrl = "http://localhost:9000";
    
    /**
	 * Client web application's base URL.
	 * Used in the verification link mailed to the users, etc.
	 */
    private String afterOauth2LoginUrl = "http://localhost:9000/after-social-login";

    /**
     * Secret string used for encrypting remember-me tokens
     */
    @Size(min=6)
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
	 * Properties related to the initial Admin user to be created
	 */
	private Admin admin = new Admin();
	
	
	/**
	 * Social login related properties
	 */
	private List<ClientResource> clientResources;
	
	
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

	public String getAfterOauth2LoginUrl() {
		return afterOauth2LoginUrl;
	}

	public void setAfterOauth2LoginUrl(String afterOauth2LoginUrl) {
		this.afterOauth2LoginUrl = afterOauth2LoginUrl;
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

    public List<ClientResource> getClientResources() {
		return clientResources;
	}

	public void setClientResources(List<ClientResource> clientResources) {
		this.clientResources = clientResources;
	}


	
	/**************************
	 * Static classes
	 *************************/


	/**
     * Recaptcha related properties
     */
	public static class Recaptcha {
    	
		/**
         * Google ReCaptcha Site Key
         */
    	private String sitekey;
    	        
        /**
         * Google ReCaptcha Secret Key
         */
    	private String secretkey;

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
		 * Comma separated whitelisted URLs for CORS.
		 * Should contain the applicationURL at the minimum.
		 * Not providing this property would disable CORS configuration.
		 */
		private String[] allowedOrigins;
		
		/**
		 * Methods to be allowed, e.g. GET,POST,...
		 */
		private String[] allowedMethods = {"GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "OPTIONS", "PATCH"};
		
		/**
		 * Request headers to be allowed, e.g. content-type,accept,origin,x-requested-with,x-xsrf-token,...
		 */
		private String[] allowedHeaders = {
				"Accept",
				"Accept-Encoding",
				"Accept-Language",
				"Cache-Control",
				"Connection",
				"Content-Length",
				"Content-Type",
				"Cookie",
				"Host",
				"Origin",
				"Pragma",
				"Referer",
				"User-Agent",
				"x-requested-with",
				LemonSecurityConfig.XSRF_TOKEN_HEADER_NAME};
		
		/**
		 * Response headers that you want to expose to the client JavaScript programmer, e.g. "X-XSRF-TOKEN".
		 * I don't think we need to mention here the headers that we don't want to access through JavaScript.
		 * Still, by default, we have provided most of the common headers.
		 *  
		 * <br>
		 * See <a href="http://stackoverflow.com/questions/25673089/why-is-access-control-expose-headers-needed#answer-25673446">
		 * here</a> to know why this could be needed.
		 */		
		private String[] exposedHeaders = {
				"Cache-Control",
				"Connection",
				"Content-Type",
				"Date",
				"Expires",
				"Pragma",
				"Server",
				"Set-Cookie",
				"Transfer-Encoding",
				"X-Content-Type-Options",
				"X-XSS-Protection",
				"X-Frame-Options",
				"X-Application-Context",
				LemonSecurityConfig.XSRF_TOKEN_HEADER_NAME};
		
		/**
		 * CORS <code>maxAge</code> long property
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
	 * Properties regarding the initial Admin user to be created
	 * 
	 * @author Sanjay Patel
	 */
	public static class Admin {
		
		/**
		 * Login ID of the initial Admin user to be created 
		 */
		private String username;
		
		/**
		 * Password of the initial Admin user to be created 
		 */		
		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}		
	}
	
	public static class ClientResource {
		
		private String name;
		private AuthorizationCodeResourceDetails client;
		private ResourceServerProperties resource;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public AuthorizationCodeResourceDetails getClient() {
			return client;
		}
		public void setClient(AuthorizationCodeResourceDetails client) {
			this.client = client;
		}
		public ResourceServerProperties getResource() {
			return resource;
		}
		public void setResource(ResourceServerProperties resource) {
			this.resource = resource;
		}	
	}

	
}
