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

package com.naturalprogrammer.spring.lemon.commonsweb.exceptions.handlers;

import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;
import com.naturalprogrammer.spring.lemon.exceptions.MultiErrorException;
import com.naturalprogrammer.spring.lemon.exceptions.handlers.AbstractExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingPathVariableException;

import java.util.Collection;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MissingPathVariableExceptionHandler extends AbstractExceptionHandler<MissingPathVariableException> {

	public MissingPathVariableExceptionHandler() {
		
		super(MissingPathVariableException.class);
		log.info("Created");
	}
	
	@Override
	public HttpStatus getStatus(MissingPathVariableException ex) {
		return HttpStatus.NOT_FOUND;
	}
}
