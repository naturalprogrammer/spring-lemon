package com.naturalprogrammer.spring.lemondemo;

import org.springframework.stereotype.Component;

import com.naturalprogrammer.spring.lemondemo.controllers.MyController;

@Component
public class UserClient {

	public static String USER_URI = MyController.BASE_URI + "/users";
	
}
