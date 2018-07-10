package com.naturalprogrammer.spring.lemondemo.services;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemonreactive.LemonReactiveService;

@Service
public class MyService extends LemonReactiveService<User, ObjectId> {

}
