package com.naturalprogrammer.spring.lemon.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import com.naturalprogrammer.spring.lemon.domain.VersionedEntity;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;

/**
 * Needed to check the permission for the service methods
 * annotated with @PreAuthorize("hasPermission(...
 * 
 * @author Sanjay Patel
 *
 * @param <U>	The user class
 * @param <ID>	Primary key class, e.g. Long
 */
public class LemonPermissionEvaluator implements PermissionEvaluator {

	private static final Log log = LogFactory.getLog(LemonPermissionEvaluator.class);

	public LemonPermissionEvaluator() {
		log.info("Created");
	}

	/**
	 * Called by Spring Security to evaluate the permission
	 * 
	 * @param auth	Spring Security authentication object,
	 * 				from which the current-user can be found
	 * @param targetDomainObject	Object for which permission is being checked
	 * @param permission			What permission is being checked for, e.g. 'edit'
	 */
	@Override
	public boolean hasPermission(Authentication auth,
			Object targetDomainObject, Object permission) {
		
		log.debug("Checking whether " + auth
			+ "\n  has " + permission + " permission for "
			+ targetDomainObject);
		
		if (targetDomainObject == null)	// if no domain object is provided,
			return true;				// let's pass, allowing the service method
										// to throw a more sensible error message
		
		// Let's delegate to the entity's hasPermission method
		VersionedEntity<?, ?> entity = (VersionedEntity<?, ?>) targetDomainObject;
		return entity.hasPermission(LemonUtils.getSpringUser(auth), (String) permission);
	}

	
	/**
	 * We need to override this method as well. To keep things simple,
	 * Let's not use this, throwing exception is someone uses it.
	 */
	@Override
	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {
		
		throw new UnsupportedOperationException("hasPermission() by ID is not supported");		
	}

}
