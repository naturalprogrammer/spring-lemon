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

package com.naturalprogrammer.spring.lemon.commons.domain;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import java.io.Serializable;
import java.util.Optional;

/**
 * Needed for auto-filling of the
 * AbstractAuditable columns of AbstractUser
 *  
 * @author Sanjay Patel
 */
public abstract class AbstractAuditorAware<ID extends Serializable>
implements AuditorAware<ID> {

    private static final Log log = LogFactory.getLog(AbstractAuditorAware.class);
    
    private IdConverter<ID> idConverter;
    
    @Autowired
	public void setIdConverter(IdConverter<ID> idConverter) {
		
		this.idConverter = idConverter;
		log.info("Created");
	}

	protected abstract UserDto currentUser();
	
	@Override
	public Optional<ID> getCurrentAuditor() {
		
		UserDto user = currentUser();
		
		if (user == null)
			return Optional.empty();
		
		return Optional.of(idConverter.toId(user.getId()));
	}	
}
