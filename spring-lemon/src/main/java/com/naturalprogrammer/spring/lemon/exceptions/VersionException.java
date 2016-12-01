package com.naturalprogrammer.spring.lemon.exceptions;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;

/**
 * Version exception, to be thrown when concurrent
 * updates of an entity is noticed. 
 * 
 * @author Sanjay Patel
 */
public class VersionException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	public VersionException(String entityName) {
		super(LemonUtil.getMessage(
			"com.naturalprogrammer.spring.versionException", entityName));
	}
}
