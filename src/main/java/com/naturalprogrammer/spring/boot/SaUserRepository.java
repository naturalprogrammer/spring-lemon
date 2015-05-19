package com.naturalprogrammer.spring.boot;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * See http://stackoverflow.com/questions/27545276/how-to-implement-a-spring-data-repository-for-a-mappedsuperclass
 * @author skpat_000
 *
 * @param <U>
 */
public abstract interface SaUserRepository<U extends SaUser> extends JpaRepository<U, Long> {
	
	U findByEmail(String email);

	U findByForgotPasswordCode(String forgotPasswordCode);

}
