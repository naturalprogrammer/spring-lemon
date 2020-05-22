package com.naturalprogrammer.spring.lemon.commons.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#user, 'edit')")
public @interface UserEditPermission {}