package com.naturalprogrammer.spring.boot.domain;

import java.io.Serializable;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.boot.util.LemonUtil;

@Component
public class AuditorAwareImpl
	<U extends AbstractUser<U,ID>,
	 ID extends Serializable>
implements AuditorAware<U> {

	public U getCurrentAuditor() {
		return LemonUtil.getLoggedInUser();
	}
	
}
