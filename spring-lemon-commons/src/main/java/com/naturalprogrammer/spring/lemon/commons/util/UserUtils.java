/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.spring.lemon.commons.util;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
