package com.naturalprogrammer.spring.lemonreactive.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

import java.io.Serializable;

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
}
