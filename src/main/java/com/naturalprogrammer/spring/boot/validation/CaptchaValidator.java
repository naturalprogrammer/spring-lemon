package com.naturalprogrammer.spring.boot.validation;

import java.util.Collection;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.naturalprogrammer.spring.boot.SaUser;
import com.naturalprogrammer.spring.boot.SaUserRepository;
import com.naturalprogrammer.spring.boot.SaUtil;

/**
 * Reference
 *   http://www.captaindebug.com/2011/07/writng-jsr-303-custom-constraint_26.html#.VIVhqjGUd8E
 *   http://www.captechconsulting.com/blog/jens-alm/versioned-validated-and-secured-rest-services-spring-40-2?_ga=1.71504976.2113127005.1416833905
 * 
 * @author skpat_000
 *
 */
@Component
public class CaptchaValidator implements ConstraintValidator<Captcha, String> {
	
	private final Log log = LogFactory.getLog(getClass());

	private static class RequestData {
		
		private String secret;
		private String response;
		
		public String getSecret() {
			return secret;
		}
		public void setSecret(String secret) {
			this.secret = secret;
		}
		public String getResponse() {
			return response;
		}
		public void setResponse(String response) {
			this.response = response;
		}

	}
	
	private static class ResponseData {
		
		private boolean success;
		private Collection<String> errorCodes;
		
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public Collection<String> getErrorCodes() {
			return errorCodes;
		}
		public void setErrorCodes(Collection<String> errorCodes) {
			this.errorCodes = errorCodes;
		}
	}
	
	@Value(SaUtil.RECAPTCHA_SECRET_KEY)
	private String reCaptchaSecretKey;
	
	@Resource
	private RestTemplate restTemplate;
	
	@Override
	public boolean isValid(String captchaResponse, ConstraintValidatorContext context) {
		
	    /**
	     * Refer http://www.journaldev.com/7133/how-to-integrate-google-recaptcha-in-java-web-application  
	     */
		
		if (captchaResponse == null || "".equals(captchaResponse))
	         return false;
	        
		RequestData requestData = new RequestData();
		requestData.setResponse(captchaResponse);
		requestData.setSecret(reCaptchaSecretKey);
		
		try {
			ResponseData responseData = restTemplate.postForObject("https://www.google.com/recaptcha/api/siteverify",
				requestData, ResponseData.class);
			return responseData.success;			
		} catch (Throwable t) {
			log.error(ExceptionUtils.getStackTrace(t));
			return false;
		}
		
	}

	@Override
	public void initialize(Captcha constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

}
