package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import lombok.Getter;
import lombok.Setter;


/**
 * Base class for all entities needing optimistic locking.
 * 
 * @author Sanjay Patel
 */
@MappedSuperclass
@Getter @Setter
public abstract class VersionedEntity<U extends AbstractUser<U,ID>, ID extends Serializable> extends LemonEntity<U, ID> {

	private static final long serialVersionUID = 4310555782328370192L;
	
	@Version
	private Long version;
}
