package com.naturalprogrammer.spring.boot;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.jpa.domain.AbstractAuditable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@MappedSuperclass
@JsonIgnoreProperties({ "createdBy", "lastModifiedBy" })
public abstract class VersionedEntity<U extends BaseUser<U,ID>, ID extends Serializable> extends AbstractAuditable<U, ID> {

	private static final long serialVersionUID = 4310555782328370192L;
	
	@Version
	private long version;

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public boolean hasPermission(U loggedInUser, String permission) {
		// override this in subclasses
		return false;
	}
	
}
