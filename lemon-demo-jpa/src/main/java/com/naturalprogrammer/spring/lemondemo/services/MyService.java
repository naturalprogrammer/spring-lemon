package com.naturalprogrammer.spring.lemondemo.services;

import java.util.Map;

import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.lemon.LemonService;
import com.naturalprogrammer.spring.lemon.security.UserDto;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.naturalprogrammer.spring.lemondemo.entities.User;

@Service
public class MyService extends LemonService<User, Long> {

	public static final String ADMIN_NAME = "Administrator";

	@Override
    public User newUser() {
        return new User();
    }

	@Override
    protected void updateUserFields(User user, User updatedUser, UserDto<Long> currentUser) {

        super.updateUserFields(user, updatedUser, currentUser);

        user.setName(updatedUser.getName());

        LemonUtils.afterCommit(() -> {
            if (currentUser.getId().equals(user.getId()))
                currentUser.setTag(user.toTag());
        });
    }
    
    @Override
    protected User createAdminUser() {
    	
    	User user = super.createAdminUser(); 
    	user.setName(ADMIN_NAME);
    	return user;
    }
    
    
    @Override
    public void fillAdditionalFields(String registrationId, User user, Map<String, Object> attributes) {
    	
    	String nameKey;
    	
    	switch (registrationId) {
    		
    	case "facebook":
    		nameKey = StandardClaimNames.NAME;
    		break;
    		
    	case "google":
			nameKey = StandardClaimNames.NAME;
			break;
			
		default:
			throw new UnsupportedOperationException("Fetching name from " + registrationId + " login not supprrted");
    	}
    	
    	user.setName((String) attributes.get(nameKey));
    }
}