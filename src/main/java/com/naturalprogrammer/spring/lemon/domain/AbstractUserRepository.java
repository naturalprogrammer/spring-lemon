package com.naturalprogrammer.spring.lemon.domain;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * See http://stackoverflow.com/questions/27545276/how-to-implement-a-spring-data-repository-for-a-mappedsuperclass
 * @author Sanjay Patel
 *
 * @param <U>
 */
@NoRepositoryBean
public abstract interface AbstractUserRepository
	<U extends AbstractUser<U,ID>, ID extends Serializable>
extends JpaRepository<U, ID> {
	
	Optional<U> findByEmail(String email);
	
	Optional<U> findByForgotPasswordCode(String forgotPasswordCode);
}
