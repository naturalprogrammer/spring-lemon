package com.naturalprogrammer.spring.boot;

import java.io.Serializable;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl<U extends BaseUser<U,ID>, ID extends Serializable> implements AuditorAware<U> {

	public U getCurrentAuditor() {

		return SaUtil.getLoggedInUser();
		
	}
		
}