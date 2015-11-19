# Spring Lemon

When developing a *REST API* using Spring Framework for your *single page* or *mobo* applications, you will need to do many subtle configurations. You will also need to lay out some good patterns, base classes and utilities to elegantly handle validation, security etc.

All these configurations and code would be common to all your applications. Not only that, most of the user module, e.g. sign up, change password etc. would also be common.

How about someone writing this code as a configurable and extensible library, which you can include in your project and start coding your business logic straightaway? This way, you not only get rid of coding this techie stuff, but also get rid of keeping it updated along with the new releases of Spring.

Welcome to *Spring Lemon*. It has all the essential configurations, patterns, base classes and utilities for developing REST APIs using Spring Boot 1.3 or above. It also has a production-grade extensible user module with features like *sign up, sign in, verify email, update profile, forgot password, change password, change email, captcha validation* etc..

Most Spring Boot applications can use Spring Lemon straightaway, with some simple configurations. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor to this library!

## Documentation and Resources

1. [Getting started guide](https://www.gitbook.com/book/naturalprogrammer/spring-lemon-getting-started/details) - It will familiarize you with Spring Lemon and the core API you inherit when you use it.
1. [Lemon Demo application](https://github.com/naturalprogrammer/lemon-demo) - A sample application using Spring Lemon. Quite similar to the one developed in the above [getting started guide](https://www.gitbook.com/book/naturalprogrammer/spring-lemon-getting-started/details), but additionally has automated tests.
1. [Demo Angular 1.x front-end application](https://github.com/naturalprogrammer/lemon-demo-angular1) - A demo AngularJS 1.x front-end. It'll work both for the application developed in the above [getting started guide](https://www.gitbook.com/book/naturalprogrammer/spring-lemon-getting-started/details) as well as the [Lemon Demo application](https://github.com/naturalprogrammer/lemon-demo). 
1. [Spring Framework REST API Development - A Complete Blueprint](https://gumroad.com/l/exuo) - An e-book discussing Spring Lemon internals, aiming to serve as a complete blueprint for developing Spring Boot REST APIs, whether you use Spring Lemon or not. After going through this book, you will be able to use Spring Lemon fluently, fork and customize it if needed, or develop another such library from scratch.

## Help and Support
1. Community help is available at [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-lemon), under the `spring-lemon` tag. DO NOT FORGET TO TAG YOUR QUESTIONS WITH `spring-lemon`!  
1. [Submit an issue](https://github.com/naturalprogrammer/spring-lemon/issues) for any bug or enhancement. Please check first that the issue isn't already reported earlier.
1. Training and professional help is provided by [naturalprogrammer.com](http://www.naturalprogrammer.com).

## Releases

1. See [here](https://github.com/naturalprogrammer/spring-lemon/releases).
