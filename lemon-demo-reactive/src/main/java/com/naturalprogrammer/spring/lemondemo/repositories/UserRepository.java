package com.naturalprogrammer.spring.lemondemo.repositories;
import org.bson.types.ObjectId;

import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;

public interface UserRepository extends AbstractMongoUserRepository<User, ObjectId> {

}
