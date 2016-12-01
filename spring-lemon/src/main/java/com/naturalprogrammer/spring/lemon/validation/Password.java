package com.naturalprogrammer.spring.lemon.validation;

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

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;

/**
 * Annotation for password constraint
 * 
 * @see <a href="http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#example-composed-constraint">Composed constraint example</a>
 *  
 * @author Sanjay Patel
 *
 */
@NotBlank(message="{com.naturalprogrammer.spring.blank.password}")
@Size(min=AbstractUser.PASSWORD_MIN, max=AbstractUser.PASSWORD_MAX,
	message="{com.naturalprogrammer.spring.invalid.password.size}")
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { })
public @interface Password {
	
	String message() default "{com.naturalprogrammer.spring.invalid.password.size}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
