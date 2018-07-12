package com.naturalprogrammer.spring.lemonreactive.domain;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import reactor.core.publisher.Mono;

/**
 * Abstract UserRepository interface
 *
 * @author Sanjay Patel
 */
@NoRepositoryBean
public interface AbstractMongoUserRepository
	<U extends AbstractMongoUser<ID>, ID extends Serializable>
	extends ReactiveMongoRepository<U, ID> {
	
	Mono<U> findByEmail(String email);
	U findFirstByEmail(String email);
}
