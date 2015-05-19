package com.naturalprogrammer.spring.boot;

import com.naturalprogrammer.spring.boot.security.UserDto;

public class ContextDto {

	protected UserDto userDto;

	public UserDto getUserDto() {
		return userDto;
	}

	public void setUserDto(UserDto userDto) {
		this.userDto = userDto;
	}
	
}
