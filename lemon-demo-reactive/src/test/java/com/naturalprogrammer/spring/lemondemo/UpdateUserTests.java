package com.naturalprogrammer.spring.lemondemo;

import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemondemo.domain.User;
import com.naturalprogrammer.spring.lemondemo.dto.TestUserDto;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;

import static com.naturalprogrammer.spring.lemondemo.MyTestUtils.*;
import static com.naturalprogrammer.spring.lemondemo.controllers.MyController.BASE_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateUserTests extends AbstractTests {

	private static final String UPDATED_NAME = "Edited name";
	
	@Value("classpath:/update-user/patch-update-user.json")
    private Resource userPatch;
	
	@Value("classpath:/update-user/patch-admin-role.json")
    private Resource userPatchAdminRole;
    
	@Value("classpath:/update-user/patch-null-name.json")
    private Resource userPatchNullName;

	@Value("classpath:/update-user/patch-long-name.json")
	private Resource userPatchLongName;

	/**
	 * A non-admin user should be able to update his own name,
	 * but changes in roles should be skipped.
	 * The name of security principal object should also
	 * change in the process.
	 * @throws Exception 
	 */
	@Test
    public void testUpdateSelf() throws Exception {
		
		updateUser(UNVERIFIED_USER_ID, UNVERIFIED_USER_ID, userPatch)
		.expectStatus().isOk()
			.expectHeader().exists(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
			.expectBody(TestUserDto.class)
			.consumeWith(result -> {
				
				TestUserDto userDto = result.getResponseBody();
				
				assertEquals(UNVERIFIED_USER_ID, userDto.getId());
				assertEquals(UNVERIFIED_USER_EMAIL, userDto.getUsername());
				assertEquals(UPDATED_NAME, userDto.getTag().getName());
				assertEquals(1, userDto.getRoles().size());
				assertTrue(userDto.getRoles().contains(UserUtils.Role.UNVERIFIED));
			});
		
		User user = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class).block();
		
		// Ensure that data changed properly
		assertEquals(UNVERIFIED_USER_EMAIL, user.getEmail());
		assertEquals(1, user.getRoles().size());
		assertTrue(user.getRoles().contains(UserUtils.Role.UNVERIFIED));
		assertEquals(1L, user.getVersion().longValue());
		
		// Version mismatch
		updateUser(UNVERIFIED_USER_ID, UNVERIFIED_USER_ID, userPatch)
		.expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
	
	
	/**
	 * A good ADMIN should be able to update another user's name and roles.
	 * The name of security principal object should NOT change in the process,
	 * and the verification code should get set/unset on addition/deletion of
	 * the UNVERIFIED role. 
	 * @throws Exception 
	 */
	@Test
    public void testGoodAdminCanUpdateOther() throws Exception {
		
		updateUser(UNVERIFIED_USER_ID, ADMIN_ID, userPatch)
		.expectStatus().isOk()
			.expectHeader().exists(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
			.expectBody(TestUserDto.class)
			.consumeWith(result -> {
				
				TestUserDto userDto = result.getResponseBody();
				
				assertEquals(UNVERIFIED_USER_ID, userDto.getId());
				assertEquals(UNVERIFIED_USER_EMAIL, userDto.getUsername());
				assertEquals(UPDATED_NAME, userDto.getTag().getName());
				assertEquals(1, userDto.getRoles().size());
				assertTrue(userDto.getRoles().contains(UserUtils.Role.ADMIN));
			});
		
		User user = mongoTemplate.findById(UNVERIFIED_USER_ID, User.class).block();
		
		// Ensure that data changed properly
		assertEquals(UNVERIFIED_USER_EMAIL, user.getEmail());
		assertEquals(1, user.getRoles().size());
		assertTrue(user.getRoles().contains(UserUtils.Role.ADMIN));
    }

	/**
	 * Providing an unknown id should return 404.
	 */
	@Test
    public void testUpdateUnknownId() throws Exception {
    	
		updateUser(ObjectId.get(), ADMIN_ID, userPatch)
		.expectStatus().isNotFound();
    }
	
	/**
	 * A non-admin trying to update the name and roles of another user should throw exception
	 * @throws Exception 
	 */
	@Test
    public void testUpdateAnotherUser() throws Exception {
    	
		updateUser(ADMIN_ID, UNVERIFIED_USER_ID, userPatch)
		.expectStatus().isForbidden();
    }

	/**
	 * A bad ADMIN trying to update the name and roles of another user should throw exception
	 * @throws Exception 
	 */
	@Test
    public void testBadAdminUpdateAnotherUser() throws Exception {
		
		updateUser(UNVERIFIED_USER_ID, UNVERIFIED_ADMIN_ID, userPatch)
		.expectStatus().isForbidden();

		updateUser(UNVERIFIED_USER_ID, BLOCKED_ADMIN_ID, userPatch)
		.expectStatus().isForbidden();
	}
	
	/**
	 * A good ADMIN should not be able to change his own roles
	 * @throws Exception 
	 */
	@Test
    public void goodAdminCanNotUpdateSelfRoles() throws Exception {
    	
		updateUser(ADMIN_ID, ADMIN_ID, userPatchAdminRole)
		.expectStatus().isOk()
			.expectHeader().exists(LecUtils.TOKEN_RESPONSE_HEADER_NAME)
			.expectBody(TestUserDto.class)
			.consumeWith(result -> {
				
				TestUserDto userDto = result.getResponseBody();
				
				assertEquals(UPDATED_NAME, userDto.getTag().getName());
				assertEquals(1, userDto.getRoles().size());
				assertTrue(userDto.getRoles().contains(UserUtils.Role.ADMIN));
			});
		
		User user = mongoTemplate.findById(ADMIN_ID, User.class).block();
		
		assertEquals(1, user.getRoles().size());
    }

	/**
	 * Invalid name
	 * @throws Exception 
	 */
	@Test
    public void testUpdateUserInvalidNewName() throws Exception {
    	
		// Null name
		updateUser(UNVERIFIED_USER_ID, ADMIN_ID, userPatchNullName)
		.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

		// Too long name
		updateUser(UNVERIFIED_USER_ID, UNVERIFIED_USER_ID, userPatchLongName)
		.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

	private ResponseSpec updateUser(ObjectId userId, ObjectId loggedInId, Resource patch) {
		
		return CLIENT.patch().uri(BASE_URI + "/users/{id}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, TOKENS.get(loggedInId))
				.body(BodyInserters.fromResource(patch))
			.exchange();

	}
}
