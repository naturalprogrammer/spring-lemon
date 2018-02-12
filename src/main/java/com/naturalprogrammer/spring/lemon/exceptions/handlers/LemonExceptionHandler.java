//package com.naturalprogrammer.spring.lemon.exceptions.handlers;
//
//import java.util.Collection;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//
//import com.naturalprogrammer.spring.lemon.validation.FieldError;
//
//public interface LemonExceptionHandler<T extends Throwable> {
//	
//	String getExceptionName();
//	
//	String getMessage(T ex);
//	HttpStatus getStatus(T ex);
//	Collection<FieldError> getErrors(T ex);
//	
//	//void putErrorDetails(Map<String, Object> errorAttributes, T ex);
//}
