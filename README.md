# Spring Lemon (under construction)

When developing a *Single Page Application* or a *REST API* using Spring Framework and Spring Boot, we need several subtle configurations. We also need laying out some good patterns to handle validation, security etc. in an elegant manner.

These configurations and patterns could be reused in most of the applications, maybe with minor tweaks. How about someone writing this common code as a configurable and extensible library, which you can include in your *pom* and start coding your business logic rightaway? Or, you can at least learn the best practices from the library, and use those in your code?

Welcome to *Spring Lemon*. It has all the essential configurations and patterns for developing single page application (SPA) backends and REST APIs using Spring Boot. It also has a production-ready user module with features like sign up, sign in, validate email, change password, etc., which you can use in your applications. For most of the applications, Spring Lemon can be used without or with some customization. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor to this library!

## Limitations
Currently Spring Lemon supports only Spring Data JPA.

## Getting Started
Below are the steps to quickly develop a REST API using Spring Lemon. If you like videos, here is it.
 
### Create a new Spring Boot application

Create a new Spring Boot application. Your application *must use the same version of Spring Boot* that Spring Lemon uses. The latest version of Spring Lemon is 0.7.1, which uses Spring Boot 1.3.0.M1.

### Add Spring Lemon

Add `spring-lemon` to the dependencies section in `pom.xml` (or your gradle configuration file):

``` xml
<dependency>
    <groupId>com.naturalprogrammer.spring</groupId>
    <artifactId>lemon</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
Spring Lemon already includes many dependencies you might just be repeating. Remove those from your pom by looking at its pom.

### Scan Spring Lemon Components

You need to scan the components not only in your application, but also in Spring Lemon, To do so, in your main class, replace the `@SpringBootApplication` annotation with

``` java
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {YourApplication.class, LemonConfig.class})
```
### Set up properties files

Spring Lemon uses some properties which you need to supply. So, copy these three files to your `src/main/resources` folder:

application.properties
messages.properties
ValidationMessages.properties

### Configuring security

Spring Lemon has all the essential configurations and classes for using Spring Security. It currently supports username/password authentication, and has customized handlers ideal for SPAs and REST APIs. It also has built-in support for password encryption, CSRF, CORS remember-me, and switch-user.

To enable security with all the above features, just create a class like this in your application:

``` java
@Configuration
public class MySecurityConfig extends LemonSecurityConfig {
    
}
```
You can customize it by overriding its methods, but we won't need that for this application. For more details, refer this book *Developing Elegant Single Page Applications and REST APIs using Spring Framework*. 

### Customizing the user module

Spring Lemon has base functionality for sign up, sign in, verify user ....... Let's customize and use that in our application:

#### Creating a User entity
 
Create a User class by inheriting from `AbstractUser`, like this:

``` java
@Entity
@Table(name="usr", indexes = {
    @Index(columnList = "email", unique=true),
    @Index(columnList = "verificationCode", unique=true),
    @Index(columnList = "forgotPasswordCode", unique=true)
})
public class User extends AbstractUser<User,Long> {

    private static final long serialVersionUID = 2716710947175132319L;
    
}
```

To add a `name` property, fill it like this:

``` java
@Entity
@Table(name="usr", indexes = {
    @Index(columnList = "email", unique=true),
    @Index(columnList = "verificationCode", unique=true),
    @Index(columnList = "forgotPasswordCode", unique=true)
})
public class User extends AbstractUser<User,Long> {

    private static final long serialVersionUID = 2716710947175132319L;
    
    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;
    
    @NotBlank(message = "{required.name}")
    @Size(min=NAME_MIN, max=NAME_MAX, groups = {SignUpValidation.class, UpdateValidation.class})
    @Column(nullable = false, length = NAME_MAX)
    protected String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
```

We are going to include this property in *Sign Up* and *Update Profile* forms. Hence, we have coded `groups = {SignUpValidation.class, UpdateValidation.class}` above, which tells spring Lemon to validate the property at *Sign Up* and *Update*.

