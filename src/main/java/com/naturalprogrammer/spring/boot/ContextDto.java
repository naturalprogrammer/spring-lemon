package com.naturalprogrammer.spring.boot;

import com.naturalprogrammer.spring.boot.security.UserDto;

public class ContextDto {

	protected String reCaptchaSiteKey;
	protected UserDto userDto;

	public String getReCaptchaSiteKey() {
		return reCaptchaSiteKey;
	}

	public void setReCaptchaSiteKey(String reCaptchaSiteKey) {
		this.reCaptchaSiteKey = reCaptchaSiteKey;
	}

	public UserDto getUserDto() {
		return userDto;
	}

	public void setUserDto(UserDto userDto) {
		this.userDto = userDto;
	}
	
}
