package com.naturalprogrammer.spring.lemon.exceptions;

@FunctionalInterface
public interface ExceptionCodeMaker {

	String make(Throwable t);
}
