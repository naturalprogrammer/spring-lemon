package com.naturalprogrammer.spring.lemonreactive.domain;

import java.io.Serializable;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import reactor.core.publisher.Mono;

/**
 * Abstract UserRepository interface
 * 
 * @see <a href="http://stackoverflow.com/questions/27545276/how-to-implement-a-spring-data-repository-for-a-mappedsuperclass">how-to-implement-a-spring-data-repository-for-a-mappedsuperclass</a>
 * @author Sanjay Patel
 */
@NoRepositoryBean
public interface AbstractMongoUserRepository
	<U extends AbstractMongoUser<ID>, ID extends Serializable>
	extends MongoRepository<U, ID> {
	
	Mono<U> findByEmail(String email);
}
