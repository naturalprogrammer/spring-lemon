package com.naturalprogrammer.spring.lemon.commons.domain;

import java.io.Serializable;
import java.util.Set;

public interface LemonUser<ID extends Serializable> {

	void setEmail(String username);
	void setPassword(String password);
	Set<String> getRoles();
	String getPassword();
	void setCredentialsUpdatedMillis(long currentTimeMillis);
	ID getId();
	String getEmail();

}
