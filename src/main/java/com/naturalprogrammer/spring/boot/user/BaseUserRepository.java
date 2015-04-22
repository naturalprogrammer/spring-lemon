package com.naturalprogrammer.spring.boot.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * See http://stackoverflow.com/questions/27545276/how-to-implement-a-spring-data-repository-for-a-mappedsuperclass
 * @author skpat_000
 *
 * @param <U>
 */
public abstract interface BaseUserRepository<U extends BaseUser> extends JpaRepository<U, Long> {
	
	BaseUser findByEmail(String email);

	BaseUser findByForgotPasswordCode(String forgotPasswordCode);

}
