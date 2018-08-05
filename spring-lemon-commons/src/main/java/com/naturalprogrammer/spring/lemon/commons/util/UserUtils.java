package com.naturalprogrammer.spring.lemon.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;

public class UserUtils {

	private static final Log log = LogFactory.getLog(UserUtils.class);

	public static final int EMAIL_MIN = 4;
	public static final int EMAIL_MAX = 250;
	public static final int UUID_LENGTH = 36;
	public static final int PASSWORD_MAX = 50;
	public static final int PASSWORD_MIN = 6;

	/**
	 * Role constants. To allow extensibility, this couldn't be made an enum
	 */
	public interface Role {

		static final String UNVERIFIED = "UNVERIFIED";
		static final String BLOCKED = "BLOCKED";
		static final String ADMIN = "ADMIN";
	}

	public interface Permission {

		static final String EDIT = "edit";
	}

	// validation groups
	public interface SignUpValidation {
	}

	public interface UpdateValidation {
	}

	public interface ChangeEmailValidation {
	}

	// JsonView for Sign up
	public interface SignupInput {
	}

	public static <ID> boolean hasPermission(ID id, UserDto currentUser, String permission) {

		log.debug("Computing " + permission + " permission for User " + id + "\n  Logged in user: " + currentUser);

		if (permission.equals("edit")) {

			if (currentUser == null)
				return false;

			boolean isSelf = currentUser.getId().equals(id.toString());
			return isSelf || currentUser.isGoodAdmin(); // self or admin;
		}

		return false;
	}
}
