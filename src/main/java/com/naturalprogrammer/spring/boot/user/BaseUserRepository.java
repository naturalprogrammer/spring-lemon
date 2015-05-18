package com.naturalprogrammer.spring.boot.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naturalprogrammer.spring.boot.entities.User;

/**
 * See http://stackoverflow.com/questions/27545276/how-to-implement-a-spring-data-repository-for-a-mappedsuperclass
 * @author skpat_000
 *
 * @param <U>
 */
public abstract interface BaseUserRepository<U extends User> extends JpaRepository<U, Long> {
	
	User findByEmail(String email);

	User findByForgotPasswordCode(String forgotPasswordCode);

}
