package com.naturalprogrammer.spring.lemondemo.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalprogrammer.spring.lemon.LemonController;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@RestController
@RequestMapping(MyController.BASE_URI)
public class MyController extends LemonController<User, Long> {
	
	public static final String BASE_URI = "/api/core";

}