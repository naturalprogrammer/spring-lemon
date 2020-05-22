package com.naturalprogrammer.spring.lemon.commonsmongo;

import com.naturalprogrammer.spring.lemon.exceptions.VersionException;

import java.io.Serializable;
import java.util.Objects;

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
		
		if (!Objects.equals(original.getVersion(), updated.getVersion()))
			throw new VersionException(original.getClass().getSimpleName(), original.getId().toString());
	}

}
