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

package com.naturalprogrammer.spring.lemon.commons.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validator for RetypePassword constraint
 * 
 * @see <a href="http://docs.jboss.org/hibernate/validator/4.1/reference/en-US/html/validator-usingvalidator.html#d0e326">Hibernate Validator</a>
 * @see <a href="http://docs.jboss.org/hibernate/validator/4.1/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator">Custom constraints</a>
 * 
 * @author Sanjay Patel
 */
public class RetypePasswordValidator
implements ConstraintValidator<RetypePassword, RetypePasswordForm> {
	
	private static final Log log = LogFactory.getLog(RetypePasswordValidator.class);

	@Override
	public boolean isValid(RetypePasswordForm retypePasswordForm,
		ConstraintValidatorContext context) {
		
		if (!Objects.equals(retypePasswordForm.getPassword(),
				            retypePasswordForm.getRetypePassword())) {
			
			log.debug("Retype password validation failed.");
			
			// Moving the error from form-level to
			// field-level properties: password, retypePassword
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
						"{com.naturalprogrammer.spring.different.passwords}")
						.addPropertyNode("password").addConstraintViolation()
				   .buildConstraintViolationWithTemplate(
						"{com.naturalprogrammer.spring.different.passwords}")
						.addPropertyNode("retypePassword").addConstraintViolation();
			
			return false;	
		}
		
		log.debug("Retype password validation succeeded.");		
		return true;
	}
}
