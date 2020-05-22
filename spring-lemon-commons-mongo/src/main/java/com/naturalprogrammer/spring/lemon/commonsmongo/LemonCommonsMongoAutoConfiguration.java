package com.naturalprogrammer.spring.lemon.commonsmongo;

import com.naturalprogrammer.spring.lemon.commonsreactive.LemonCommonsReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@AutoConfigureBefore({
	MongoReactiveAutoConfiguration.class,
	LemonCommonsReactiveAutoConfiguration.class})
public class LemonCommonsMongoAutoConfiguration {

}
