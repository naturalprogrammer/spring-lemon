package com.naturalprogrammer.spring.boot.context;

import com.naturalprogrammer.spring.boot.security.UserData;

public class Context {

	protected UserData userData;

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
	
}
