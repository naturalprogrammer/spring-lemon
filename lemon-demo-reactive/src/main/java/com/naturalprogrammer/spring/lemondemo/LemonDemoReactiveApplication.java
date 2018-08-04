package com.naturalprogrammer.spring.lemondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;

@SpringBootApplication
public class LemonDemoReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(LemonDemoReactiveApplication.class, args);
	}	
}
