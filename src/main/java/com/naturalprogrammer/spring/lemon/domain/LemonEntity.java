package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.domain.AbstractAuditable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.naturalprogrammer.spring.lemon.security.SpringUser;

/**
 * Base class for all entities.
 * 
 * @author Sanjay Patel
 *
 * @param <U>	the concrete user type, e.g. User
 * @param <ID>	the concrete primary key type, e.g. Long
 */
@MappedSuperclass
@JsonIgnoreProperties({ "createdBy", "lastModifiedBy", "createdDate", "lastModifiedDate" })
public class LemonEntity<U extends AbstractUser<U,ID>, ID extends Serializable> extends AbstractAuditable<U, ID> {

	private static final long serialVersionUID = -8151190931948396443L;
	
	/**
	 * Whether the given user has the given permission for
	 * this entity. Override this method where you need.
	 */
	public boolean hasPermission(SpringUser<?> user, String permission) {
		return false;
	}
	
}
