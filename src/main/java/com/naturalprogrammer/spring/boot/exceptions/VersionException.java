package com.naturalprogrammer.spring.boot.exceptions;

import com.naturalprogrammer.spring.boot.util.SaUtil;


public class VersionException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	public VersionException(String className) {
		super(SaUtil.getMessage("versionException", className));
	}
	
}
