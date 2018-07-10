package com.naturalprogrammer.spring.lemondemo.controllers;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemonreactive.LemonReactiveController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/core")
public class MyController extends LemonReactiveController<User, ObjectId> {

	@Override
	public Mono<User> signup(@RequestBody Mono<User> user, ServerWebExchange response) {
		
		return super.signup(user, response);
	}
}
