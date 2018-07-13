package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.domain.AbstractAuditable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.naturalprogrammer.spring.lemon.commons.security.PermissionEvaluatorEntity;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;

/**
 * Base class for all entities.
 * 
 * @author Sanjay Patel
  */
@MappedSuperclass
@JsonIgnoreProperties({ "createdBy", "lastModifiedBy", "createdDate", "lastModifiedDate" })
public class LemonEntity<U extends AbstractUser<U,ID>, ID extends Serializable> extends AbstractAuditable<U, ID> implements PermissionEvaluatorEntity {

	private static final long serialVersionUID = -8151190931948396443L;
	
	/**
	 * Whether the given user has the given permission for
	 * this entity. Override this method where you need.
	 */
	@Override
	public boolean hasPermission(UserDto<?> user, String permission) {
		return false;
	}
	
}
