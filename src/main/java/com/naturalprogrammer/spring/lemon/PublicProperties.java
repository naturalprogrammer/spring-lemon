package com.naturalprogrammer.spring.lemon;

import java.util.Map;

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
@ConfigurationProperties(prefix="lemon")
public class PublicProperties {
	
    private String applicationUrl;

    private String reCaptchaSiteKey;
    
	private Map<String, Object> others;

	public String getApplicationUrl() {
		return applicationUrl;
	}

	@Value("${lemon.application.url:http://localhost:9000}")
	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}

	public String getReCaptchaSiteKey() {
		return reCaptchaSiteKey;
	}

	@Value("${lemon.reCaptcha.siteKey:no key found}")
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
