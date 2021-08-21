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

package com.naturalprogrammer.spring.lemon.commons.security;

import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A lighter User class,
 * mainly used for holding logged-in user data 
 */
@Getter @Setter @ToString
public class UserDto implements Serializable {

	private static final long serialVersionUID = -9134054705405149534L;
	
	private String id;
	private String username;
	private String password;
	private Set<String> roles = new HashSet<>();
	private Serializable tag;
	
	private boolean unverified = false;
	private boolean blocked = false;
	private boolean admin = false;
	private boolean goodUser = false;
	private boolean goodAdmin = false;
	
	public void initialize() {
		
		unverified = roles.contains(UserUtils.Role.UNVERIFIED);
		blocked = roles.contains(UserUtils.Role.BLOCKED);
		admin = roles.contains(UserUtils.Role.ADMIN);
		goodUser = !(unverified || blocked);
		goodAdmin = goodUser && admin;
	}
}
