package com.naturalprogrammer.spring.lemondemo.repositories;

import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUserRepository;
import org.bson.types.ObjectId;

public interface UserRepository extends AbstractMongoUserRepository<User, ObjectId> {

}
