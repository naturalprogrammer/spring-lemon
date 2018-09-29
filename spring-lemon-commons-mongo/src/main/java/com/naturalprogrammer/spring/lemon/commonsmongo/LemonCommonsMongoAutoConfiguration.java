package com.naturalprogrammer.spring.lemon.commonsmongo;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import com.naturalprogrammer.spring.lemon.commonsreactive.LemonCommonsReactiveAutoConfiguration;

@Configuration
@EnableMongoAuditing
@AutoConfigureBefore({
	MongoReactiveAutoConfiguration.class,
	LemonCommonsReactiveAutoConfiguration.class})
public class LemonCommonsMongoAutoConfiguration {

}
