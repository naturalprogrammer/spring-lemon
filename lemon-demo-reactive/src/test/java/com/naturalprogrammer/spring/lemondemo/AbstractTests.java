package com.naturalprogrammer.spring.lemondemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import com.naturalprogrammer.spring.lemon.exceptions.ErrorResponse;
import com.naturalprogrammer.spring.lemon.exceptions.LemonFieldError;

@RunWith(SpringRunner.class)
@SpringBootTest({
	"logging.level.com.naturalprogrammer=DEBUG", // logging.level.root=ERROR does not work: https://stackoverflow.com/questions/49048298/springboottest-not-overriding-logging-level
	"logging.level.org.springframework=ERROR",
	"lemon.recaptcha.sitekey=",
	"spring.data.mongodb.database=lemontest"
})
public abstract class AbstractTests {
	
	@Autowired
	protected MongoTemplate mongoTemplate;

	@Autowired
	protected MyTestUtils testUtils;
	
	@Before
	public void initialize() {
		
		testUtils.initDatabase();
	}
	

	protected void assertErrors(EntityExchangeResult<ErrorResponse> errorResponseResult, String... fields) {
		
		ErrorResponse response = errorResponseResult.getResponseBody();
		assertEquals(fields.length, response.getErrors().size());
		
		assertTrue(response.getErrors().stream()
				.map(LemonFieldError::getField).collect(Collectors.toSet())
				.containsAll(Arrays.asList(fields)));
	}
}
