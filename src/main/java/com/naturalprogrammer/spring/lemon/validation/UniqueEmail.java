package com.naturalprogrammer.spring.lemon.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;

/**
 * Reference
 *   http://www.captaindebug.com/2011/07/writng-jsr-303-custom-constraint_26.html#.VIVhqjGUd8E
 *   http://www.captechconsulting.com/blog/jens-alm/versioned-validated-and-secured-rest-services-spring-40-2?_ga=1.71504976.2113127005.1416833905
 * 
 * @author Sanjay Patel
 *
 */
@NotBlank(message = "{com.naturalprogrammer.spring.blank.email}")
@Size(min=AbstractUser.EMAIL_MIN, max=AbstractUser.EMAIL_MAX,
	message = "{com.naturalprogrammer.spring.invalid.email.size}")
@Email(message = "{com.naturalprogrammer.spring.invalid.email}")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.CONSTRUCTOR,ElementType.PARAMETER,ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy=UniqueEmailValidator.class)
public @interface UniqueEmail {
 
    String message() default "{com.naturalprogrammer.spring.duplicate.email}";

    Class[] groups() default {};
    
    Class[] payload() default {};
}
