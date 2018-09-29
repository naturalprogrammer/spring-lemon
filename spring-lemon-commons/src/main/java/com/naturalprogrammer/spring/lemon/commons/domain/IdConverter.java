package com.naturalprogrammer.spring.lemon.commons.domain;

import java.io.Serializable;

@FunctionalInterface
public interface IdConverter<ID extends Serializable> {

	ID toId(String id);
}
