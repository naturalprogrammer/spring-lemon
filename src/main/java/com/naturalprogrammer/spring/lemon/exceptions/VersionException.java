package com.naturalprogrammer.spring.lemon.exceptions;

import com.naturalprogrammer.spring.lemon.util.LemonUtil;


public class VersionException extends RuntimeException {

	private static final long serialVersionUID = 6020532846519363456L;
	
	public VersionException(String className) {
		super(LemonUtil.getMessage("versionException", className));
	}
	
}
