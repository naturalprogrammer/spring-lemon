package com.naturalprogrammer.spring.lemondemo.repositories;

import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemondemo.entities.User;

public interface UserRepository extends AbstractUserRepository<User, Long> {

}