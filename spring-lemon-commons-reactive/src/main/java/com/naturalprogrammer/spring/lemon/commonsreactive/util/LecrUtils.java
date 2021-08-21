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

package com.naturalprogrammer.spring.lemon.commonsreactive.util;

import com.github.fge.jsonpatch.JsonPatchException;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.exceptions.util.LexUtils;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

/**
 * Useful helper methods
 * 
 * @author Sanjay Patel
 */
public class LecrUtils {
	
	private static Mono<Object> NOT_FOUND_MONO;
	
	@PostConstruct
	public void postConstruct() {
		NOT_FOUND_MONO = Mono.error(LexUtils.NOT_FOUND_EXCEPTION);
	}
	
	public static Optional<HttpCookie> fetchCookie(ServerWebExchange exchange, String cookieName) {		
		return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(cookieName));
	}

	/**
	 * Gets the current-user
	 */
	public static <ID extends Serializable> Mono<Optional<UserDto>> currentUser() {
		
		return ReactiveSecurityContextHolder.getContext()
			.map(LecUtils::currentUser)
			.map(Optional::of)
			.defaultIfEmpty(Optional.empty());
	}	
		
	public static <T> Mono<T> notFoundMono() {
		return (Mono<T>) NOT_FOUND_MONO;
	}

	public static<T> T applyPatch(T originalObj, String patchString) {

		try {
			return LecUtils.applyPatch(originalObj, patchString);
		} catch (IOException | JsonPatchException e) {
			throw new RuntimeException(e);
		}
	}
}
