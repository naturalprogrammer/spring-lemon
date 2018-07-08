package com.naturalprogrammer.spring.lemondemo.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalprogrammer.spring.lemon.LemonController;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@RestController
@RequestMapping("/api/core")
public class MyController extends LemonController<User, Long> {

}