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
	 * Override this in the subclass
	 * 
	 * @param loggedInUser
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(U user, String permission) {
		return false;
	}
	
}
