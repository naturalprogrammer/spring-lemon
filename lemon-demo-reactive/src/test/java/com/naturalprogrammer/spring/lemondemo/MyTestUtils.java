package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemondemo.dto.TestUserDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Component
public class MyTestUtils {
	
	private static Log log = LogFactory.getLog(MyTestUtils.class);

	public static final ObjectId ADMIN_ID = ObjectId.get();
	public static final ObjectId UNVERIFIED_ADMIN_ID = ObjectId.get();
	public static final ObjectId BLOCKED_ADMIN_ID = ObjectId.get();

	public static final ObjectId USER_ID = ObjectId.get();
	public static final ObjectId UNVERIFIED_USER_ID = ObjectId.get();
	public static final ObjectId BLOCKED_USER_ID = ObjectId.get();

	public static final String ADMIN_EMAIL = "admin@example.com";
	public static final String ADMIN_PASSWORD = "admin!";

	public static final String USER_PASSWORD = "Sanjay99!";
	public static final String UNVERIFIED_USER_EMAIL = "unverifieduser@example.com";

	public static final Map<ObjectId, String> TOKENS = new HashMap<>(6);

	public static WebTestClient CLIENT;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	public MyTestUtils(ApplicationContext context) {
		CLIENT = WebTestClient
					.bindToApplicationContext(context)
					.configureClient()
					.responseTimeout(Duration.ofHours(1L))
					.build();
	}

    public String login(String userName, String password) {

    	return loginResponse(userName, password)
                	.expectStatus().isOk()
                	.returnResult(TestUserDto.class)
                	.getResponseHeaders()
                	.getFirst(LecUtils.TOKEN_RESPONSE_HEADER_NAME);
    }

    public ResponseSpec loginResponse(String userName, String password) {

    	return CLIENT.post()
                .uri(BASE_URI + "/login")
                .body(fromFormData("username", userName)
                		     .with("password", password))
                .exchange();
    }

    public ResponseSpec contextResponse(String token) {

		return CLIENT.get()
			.uri(BASE_URI + "/context")
				.header(HttpHeaders.AUTHORIZATION, token)
				.exchange()
				.expectStatus().isOk();
    }

    //    @Override
//	public void run(String... args) throws Exception {
//    	
//		TOKENS.put(ADMIN_ID, login(ADMIN_EMAIL, ADMIN_PASSWORD));
//		TOKENS.put(UNVERIFIED_ADMIN_ID, login("unverifiedadmin@example.com", ADMIN_PASSWORD));
//		TOKENS.put(BLOCKED_ADMIN_ID, login("blockedadmin@example.com", ADMIN_PASSWORD));
//		TOKENS.put(USER_ID, login("user@example.com", USER_PASSWORD));
//		TOKENS.put(UNVERIFIED_USER_ID, login(UNVERIFIED_USER_EMAIL, USER_PASSWORD));
//		TOKENS.put(BLOCKED_USER_ID, login("blockeduser@example.com", USER_PASSWORD));
//	}
//
	public void initDatabase() {

		mongoTemplate.dropCollection("usr").block();
		log.debug("Creating users ... ");
		
		createUser(ADMIN_ID, ADMIN_EMAIL, ADMIN_PASSWORD, "Admin 1", "ADMIN");
		createUser(UNVERIFIED_ADMIN_ID, "unverifiedadmin@example.com", ADMIN_PASSWORD, "Unverified Admin", "ADMIN", "UNVERIFIED");
		createUser(BLOCKED_ADMIN_ID, "blockedadmin@example.com", ADMIN_PASSWORD, "Blocked Admin", "ADMIN", "BLOCKED");
		createUser(USER_ID, "user@example.com", USER_PASSWORD, "User");
		createUser(UNVERIFIED_USER_ID, UNVERIFIED_USER_EMAIL, USER_PASSWORD, "Unverified User", "UNVERIFIED");
		createUser(BLOCKED_USER_ID, "blockeduser@example.com", USER_PASSWORD, "Blocked User", "BLOCKED");			

		log.debug("Created users.");
	}

	private void createUser(ObjectId id, String email, String password, String name, String... roles) {
		
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setName(name);
		user.setCredentialsUpdatedMillis(0L);
		user.setRoles(new HashSet<String>(Arrays.asList(roles)));
		
		mongoTemplate.insert(user).block();
		TOKENS.put(user.getId(), login(user.getEmail(), password));
	}
}
