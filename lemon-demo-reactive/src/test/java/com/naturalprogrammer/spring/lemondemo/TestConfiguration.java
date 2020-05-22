package com.naturalprogrammer.spring.lemondemo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class TestConfiguration {

    //@Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory);
    }
}
