package com.naturalprogrammer.spring.lemon.exceptions;

@FunctionalInterface
public interface ExceptionIdMaker {

	String make(Throwable t);
}
