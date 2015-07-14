package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.domain.AbstractAuditable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@MappedSuperclass
@JsonIgnoreProperties({ "createdBy", "lastModifiedBy" })
public class LemonEntity<U extends AbstractUser<U,ID>, ID extends Serializable> extends AbstractAuditable<U, ID> {

	private static final long serialVersionUID = -8151190931948396443L;
	
	/**
	 * Whether the given user has the given permission for
	 * this entity. Override this method where you need.
	 * 
	 * @param user
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(U user, String permission) {
		return false;
	}
	
}
