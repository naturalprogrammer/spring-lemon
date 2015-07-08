package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.VersionedEntity;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

@Component
public class LemonPermissionEvaluator<U extends AbstractUser<U,ID>, ID extends Serializable> implements PermissionEvaluator {

	private final Log log = LogFactory.getLog(getClass());

	@Override
	public boolean hasPermission(Authentication auth,
			Object targetDomainObject, Object permission) {
		
		log.debug("Checking whether " + auth
			+ "\n  has " + permission + " permission for "
			+ targetDomainObject);
		
		if (targetDomainObject == null)
			return true;
		
		VersionedEntity<U, ID> entity = (VersionedEntity<U, ID>) targetDomainObject;
		return entity.hasPermission(LemonUtil.getUser(auth), (String) permission);
	}

	@Override
	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {
		
		throw new UnsupportedOperationException("hasPermission() by ID is not supported");
		
	}

}
