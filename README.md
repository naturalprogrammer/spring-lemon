# Spring Lemon

A real-world Spring REST service will have tons of non-functional code, including

* Spring Security configuration for REST (redirection, CSRF, CORS, JSON vulnerability, remember-me, switch user etc.)
* Stateless API key authentication support, for mobo or machine clients
* Support for traditional session-cookie based login (with remember-me and CSRF protection) for web clients
* Social sign on support (using OAuth2 providers, such as Google and Facebook)
* Mechanism for handling validations and exceptions in a cross functional manner, sending precise errors to the client
* Support for elegantly using PATCH and JsonPatch to handle partial updates correctly
* A robust user module
* Test utilities and patterns

Coding the above effectively needs in-depth knowledge of the underlying framework, i.e. Spring. It also takes a lot of development time and effort, and needs to be properly maintained as new versions of Spring modules come out.

To relieve you of this non-trivial job, we thought to bring out Spring Lemon, a tiny open source library holding all these essential code and patterns. It also includes a production grade user module with features like sign up, sign in, verify email, social signup/in, update profile, forgot password, change password, change email, captcha validation, stateless token authentication etc..

Even if you don't plan to use Spring Lemon in production, it's a good example application to learn from if you plan to develop Spring REST Services, because it showcases many essential best practices.

Most Spring Boot applications can use Spring Lemon straight away, with some simple configurations. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor!

For getting started, read [this book] (https://gum.co/YOPH).

## Documentation and Resources

1. Getting started guide
 1. [Book] (https://gum.co/YOPH)
 1. Video Tutorial - Coming Soon
1. [Example application](https://github.com/naturalprogrammer/lemon-demo) - A sample application using Spring Lemon. Quite similar to the one developed in the above [getting started guide](https://gum.co/YOPH), but additionally has automated tests.
1. [API documentation](http://www.naturalprogrammer.com/spring-lemon-apidoc) of the above application.
1. [Example Angular 1.x front-end application](https://github.com/naturalprogrammer/lemon-demo-angular1) - A sample AngularJS 1.x front-end. It'll work both for the application developed in the above [getting started guide](https://gum.co/YOPH) as well as the [Lemon Demo application](https://github.com/naturalprogrammer/lemon-demo). 
1. Coming Soon - Spring Framework REST API Development - A Real-World Guide - An eBook discussing the essential details and patterns for real-world Spring Framework REST API Development.
1. Coming Soon - Spring Lemon Reference material

## Help and Support
1. Community help is available at [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-lemon), under the `spring-lemon` tag. Do not miss to tag the questions with `spring-lemon`!
1. [Submit an issue](https://github.com/naturalprogrammer/spring-lemon/issues) for any bug or enhancement. Please check first that the issue isn't already reported earlier.
1. Mentoring, training and professional help is provided by [naturalprogrammer.com](http://www.naturalprogrammer.com/consulting/).

## Releases

1. See [here](https://github.com/naturalprogrammer/spring-lemon/releases).
