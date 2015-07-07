# Spring Lemon (under construction)

When developing a *Single Page Application* or a *REST API* using Spring Framework and Spring Boot, we need several subtle configurations. We also need laying out some good patterns to handle validation, security etc. in an elegant manner.

These configurations and patterns could be reused in majority of the applications, with little tweaks. How about someone writing this common code as a configurable and extensible library, which you can include in your *pom* and start coding your business logic rightaway? Or, you can at least learn the best practices from the library, and use those in your code?

Welcome to Spring Lemon. It has all the essential configurations and patterns for developing Single Page Application backends and REST APIs. It also has a production-ready user module with features like sign up, sign in, validate email, change password, etc., which you can use in your applications. For majority of applications, Spring Lemon can be used without or with some customization. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor to this library!

## Limitations
Currently Spring Lemon supports only Spring Data JPA.

## Getting Started

### Create a new Spring Boot application

Create your new single page application backend or REST API project as a new Spring Boot application. Your application must use the same version of Spring Boot that Spring Lemon uses. the latest version of Spring Lemon is 0.7.1, which uses Spring Boot 1.3.0.M1.

### Add Spring Lemon

Add `spring-lemon` to the dependencies section in `pom.xml` (or your gradle configuration file):

``` xml
<dependency>
    <groupId>com.naturalprogrammer.spring</groupId>
    <artifactId>lemon</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
Spring Lemon already includes many dependencies you might be repeating. Remove those from your pom by looking at its pom.

...