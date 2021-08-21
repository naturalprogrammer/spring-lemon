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

package com.naturalprogrammer.spring.lemon.commonsreactive.security;

import com.naturalprogrammer.spring.lemon.commons.domain.AbstractAuditorAware;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Needed for auto-filling of the
 * AbstractAuditable columns of AbstractUser
 *  
 * @author Sanjay Patel
 */
public class LemonReactiveAuditorAware<ID extends Serializable>
extends AbstractAuditorAware<ID> {
	
    private static final Log log = LogFactory.getLog(LemonReactiveAuditorAware.class);
    
	public LemonReactiveAuditorAware() {		
		log.info("Created");
	}

	@Override
	protected UserDto currentUser() {
		
		// TODO: Can't return a mono, as below
		// See this: https://jira.spring.io/browse/DATACMNS-1231
		// So, sorry, no reactive auditing until Spring Data supports it
		// But, if using MongoDB, you could implement a ReactiveBeforeConvertCallback:
		// https://juliuskrah.com/blog/2018/02/15/auditing-with-spring-data-jpa/#comment-4848839807

		// return LecrUtils.currentUser();
		return null;
	}	
}
