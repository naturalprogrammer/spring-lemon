package com.naturalprogrammer.spring.boot;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * See http://stackoverflow.com/questions/27545276/how-to-implement-a-spring-data-repository-for-a-mappedsuperclass
 * @author skpat_000
 *
 * @param <U>
 */
@NoRepositoryBean
public abstract interface SaUserRepository<U extends SaUser<ID>, ID extends Serializable> extends JpaRepository<U, ID> {
	
	U findByEmail(String email);

	U findByForgotPasswordCode(String forgotPasswordCode);

}
