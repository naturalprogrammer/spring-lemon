package com.naturalprogrammer.spring.lemon.commonsmongo;

import com.naturalprogrammer.spring.lemon.commons.security.PermissionEvaluatorEntity;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

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
