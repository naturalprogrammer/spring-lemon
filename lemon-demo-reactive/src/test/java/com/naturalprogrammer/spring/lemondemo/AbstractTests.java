package com.naturalprogrammer.spring.lemondemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import com.naturalprogrammer.spring.lemon.commons.mail.MailSender;
import com.naturalprogrammer.spring.lemondemo.dto.TestErrorResponse;
import com.naturalprogrammer.spring.lemondemo.dto.TestLemonFieldError;

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
	
    @MockBean
    protected MailSender<?> mailSender;

	@Before
	public void initialize() {
		
		testUtils.initDatabase();
	}
	

	protected void assertErrors(EntityExchangeResult<TestErrorResponse> errorResponseResult, String... fields) {
		
		TestErrorResponse response = errorResponseResult.getResponseBody();
		assertEquals(fields.length, response.getErrors().size());
		
		assertTrue(response.getErrors().stream()
				.map(TestLemonFieldError::getField).collect(Collectors.toSet())
				.containsAll(Arrays.asList(fields)));
	}
}
