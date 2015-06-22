package com.naturalprogrammer.spring.boot;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties those are passed to client
 * 
 * @author skpat_000
 *
 */
@Component
@ConfigurationProperties
public class PublicProperties {
	
    private String applicationUrl;

    private String reCaptchaSiteKey;
    
	private Map<String, Object> others;

	public String getApplicationUrl() {
		return applicationUrl;
	}

	@Value("${application.url:http://localhost:9000}")
	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}

	public String getReCaptchaSiteKey() {
		return reCaptchaSiteKey;
	}

	@Value("${reCaptcha.siteKey:no key found}")
	public void setReCaptchaSiteKey(String reCaptchaSiteKey) {
		this.reCaptchaSiteKey = reCaptchaSiteKey;
	}

	public Map<String, Object> getOthers() {
		return others;
	}

	public void setOthers(Map<String, Object> others) {
		this.others = others;
	}

}
