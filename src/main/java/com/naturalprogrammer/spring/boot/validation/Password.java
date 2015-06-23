package com.naturalprogrammer.spring.boot.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.naturalprogrammer.spring.boot.domain.BaseUser;

/**
 * See http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#example-composed-constraint
 * @author skpat_000
 *
 */
@NotBlank(message="{com.naturalprogrammer.spring.required.password}")
@Size(min=BaseUser.PASSWORD_MIN, max=BaseUser.PASSWORD_MAX, message="{passwordSizeError}")
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { })
public @interface Password {
	
	String message() default "{javax.validation.constraints.Size.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
