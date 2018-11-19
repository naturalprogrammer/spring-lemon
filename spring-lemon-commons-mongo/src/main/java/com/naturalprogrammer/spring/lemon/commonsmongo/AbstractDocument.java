package com.naturalprogrammer.spring.lemon.commonsmongo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.naturalprogrammer.spring.lemon.commons.security.PermissionEvaluatorEntity;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public abstract class AbstractDocument<ID extends Serializable> implements PermissionEvaluatorEntity {
	
	@Id
	protected ID id;
	
	@CreatedBy
	private ID createdBy;
	
	@CreatedDate
	private Date createdDate;
	
	@LastModifiedBy
	private ID lastModifiedBy;
	
	@LastModifiedDate
	private Date lastModifiedDate;
	
	@Version
	private Long version;
	
	/**
	 * Whether the given user has the given permission for
	 * this entity. Override this method where you need.
	 */
	@Override
	public boolean hasPermission(UserDto currentUser, String permission) {
		
		return false;		
	}

}
