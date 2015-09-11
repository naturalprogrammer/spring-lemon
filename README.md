# Spring Lemon (under construction)

When developing a *REST API* for your *single page* or *mobile applications* using Spring Framework, you will need to do many subtle configurations. You will also need to lay out some good patterns, base classes and utilities to elegantly handle validation, security etc.

All these configurations and code would be common to all your applications. Not only that, most of the user module, e.g. sign up, change password etc. would also be common.

How about someone writing this code as a configurable and extensible library, which you can include in your project and start coding your business logic straightaway? This way, you not only get rid of coding this techie stuff, but also get rid of keeping it updated along with the new releases of Spring.

Welcome to *Spring Lemon*. It has all the essential configurations, patterns, base classes and utilities for developing REST APIs using Spring Boot. It also has a production-grade extensible user module with features like sign up, sign in, verify email, forgot password, change password and update email.

Most applications can use Spring Lemon straightaway, with some simple configurations. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor to this library!

# Documentation

1. First, go through the [getting started guide](https://www.gitbook.com/book/naturalprogrammer/spring-lemon-getting-started/details). It will familiarize you with Spring Lemon and the core API you inherit when you use it. 
2. Second, read the book [REST APIs using Spring Framework - A Complete Blueprint](https://gumroad.com/naturalprogrammer#). This discusses the configurations and patterns needed for developing Spring Boot REST APIs, and the internals of Spring Lemon. After going through this book, you will be able to use Spring Lemon fluently, fork and customize it if needed, or develop another such library from scratch.
3. Later, refer to the [JavaDoc](https://javadocs.naturalprogrammer.com/rest) whenever needed.
 
# Limitations

The user module of Spring Lemon now works only with *Spring Data JPA*.
