package com.naturalprogrammer.spring.lemon.commonsmongo;

import java.io.Serializable;

import com.naturalprogrammer.spring.lemon.exceptions.VersionException;

public class LecmUtils {

	/**
	 * Throws a VersionException if the versions of the
	 * given entities aren't same.
	 * 
	 * @param original
	 * @param updated
	 */
	public static <ID extends Serializable>
	void ensureCorrectVersion(AbstractDocument<ID> original, AbstractDocument<ID> updated) {
		
		if (original.getVersion() != updated.getVersion())
			throw new VersionException(original.getClass().getSimpleName(), original.getId().toString());
	}

}
