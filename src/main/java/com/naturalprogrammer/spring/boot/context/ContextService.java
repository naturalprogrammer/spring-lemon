package com.naturalprogrammer.spring.boot.context;

import org.springframework.stereotype.Service;

import com.naturalprogrammer.spring.boot.Sa;
import com.naturalprogrammer.spring.boot.user.BaseUser;

@Service
public class ContextService {
	
	public Context getContext() {
		Context context = new Context();
		context.setUserData(Sa.getUserData());
		return context;		
	}

}
