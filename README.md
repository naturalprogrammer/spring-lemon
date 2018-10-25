# Spring Lemon

> Minimize your development cost with our intensive consulting/training — [click here](https://www.naturalprogrammer.com/) for details.

When developing **real-world** Spring REST APIs and microservices, you face many challenges like

1. How to follow a stateless and efficient security model – using JWT authentication, session sliding etc.
1. How to configure Spring Security to suit API development, e.g. returning _200_ or _401_ responses on login, configuring _CORS_, _JSON vulnerability protection_, etc.
1. How to elegantly do _validations_ and _exceptions_ and send precise errors to the client.
1. How to easily mix manual and bean validations in a single validation cycle.
1. How exactly to support multiple _social sign up/in_, using _OpenID Connect_ or _OAuth2_ providers such as _Google_ and _Facebook_.
1. How to code a robust user module (with features like _sign up_, _sign in_, _verify email_, _social sign up/in_, _update profile_, _forgot password_, _change password_, _change email_, _token authentication_ etc.) and share it across all your applications.
1. How to correctly and effeciently _secure microservices_, using long-lived and short-lived JWTs.
1. What would be good ways to _test your API_.
1. How to do _Captcha validation_.
1. How to properly organize _application properties_.
1. How to use _PATCH_ and _JsonPatch_ to handle partial updates correctly.
1. How to do all the above reactively, using WebFlux and WebFlux security.

Coding all this rightly needs in-depth knowledge of Spring. It also takes a lot of development time and effort, and needs to be properly maintained as new versions of Spring comes out.

**Spring Lemon** relieves you of all this burden. It's a set of configurable and extensible libraries, providing all above features. Use these to develop quality reactive or non-reactive monolith or microservices applications quickly and easily.  

Even if you don't plan to use Spring Lemon, it's a good example to learn from, because it showcases the essential best practices for developing elegant web services and microservices using Spring.

Most Spring Boot applications can use Spring Lemon straight away, with some simple configurations. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor!

Read [this quick starter guide](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon) or watch [this video tutorial](https://www.naturalprogrammer.com/p/spring-lemon-restful-web-services-development) for getting started.

## Libraries hierarchy

* [spring-lemon-exceptions](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Exceptions-Guide): Useful for elegant exception handling and validation in any Spring project
    * [spring-lemon-commons](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Commons-Guide): Common for all things below
        * [spring-lemon-commons-web](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Commons-Web-Guide): For developing Spring Web (non-reactive) microservices
            * [spring-lemon-commons-jpa](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Commons-JPA-Guide): For developing Spring Web (non-reactive) JPA microservices
                * [spring-lemon-jpa](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-JPA-Guide): For developing Spring Web (non-reactive) JPA monolith or auth-microservice
        * [spring-lemon-commons-reactive](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Commons-Reactive-Guide): For developing Spring WebFlux (reactive) microservices
            * [spring-lemon-commons-mongo](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Commons-MongoDB-Guide): For developing Spring WebFlux (reactive) MongoDB microservices
                * [spring-lemon-reactive](https://github.com/naturalprogrammer/spring-lemon/wiki/Spring-Lemon-Reactive-Guide): For developing Spring WebFlux (reactive) MongoDB monolith or auth-microservice

For example usages, see

* [Demo non-reactive monolith](https://github.com/naturalprogrammer/spring-lemon/tree/master/lemon-demo-jpa)
* [Demo reactive monolith](https://github.com/naturalprogrammer/spring-lemon/tree/master/lemon-demo-reactive)
* [Demo non-reactive microservices](https://github.com/naturalprogrammer/np-microservices-sample-01) and its [configuration repository](https://github.com/naturalprogrammer/np-microservices-sample-01-config)
* [Demo reactive microservices](https://github.com/naturalprogrammer/np-microservices-sample-02) and its [configuration repository](https://github.com/naturalprogrammer/np-microservices-sample-02-config)

## Documentation and Resources

> _Our [Spring Framework Recipes For Real World Application Development](https://www.naturalprogrammer.com/p/spring-framework-book-of-best-practices) — a live book discussing key real-world topics on developing Spring applications and APIs — is now available for FREE. [Click here](https://www.naturalprogrammer.com/p/spring-framework-book-of-best-practices) to get it._

1. Feature demo: https://youtu.be/6mNg-Feq8CY
1. _Getting started guide_
   1. [Book](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon)
   1. [Video Tutorial](https://www.naturalprogrammer.com/p/spring-lemon-restful-web-services-development)
1. _[Official Documentation](https://github.com/naturalprogrammer/spring-lemon/wiki)_
1. _Example applications_
    * [Demo non-reactive monolith](https://github.com/naturalprogrammer/spring-lemon/tree/master/lemon-demo-jpa)
    * [Demo reactive monolith](https://github.com/naturalprogrammer/spring-lemon/tree/master/lemon-demo-reactive)
    * [Demo non-reactive microservices](https://github.com/naturalprogrammer/np-microservices-sample-01) and its [configuration repository](https://github.com/naturalprogrammer/np-microservices-sample-01-config)
    * [Demo reactive microservices](https://github.com/naturalprogrammer/np-microservices-sample-02) and its [configuration repository](https://github.com/naturalprogrammer/np-microservices-sample-02-config)
1. _[API documentation](https://documenter.getpostman.com/view/305915/RVu2mqEH)_ of the above applications.
1. _[Example AngularJS front-end application](https://github.com/naturalprogrammer/spring-lemon/tree/master/lemon-demo-angularjs)_ — A sample AngularJS 1.x front-end. It'll work for the application developed in the above [getting started guide](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon) as well all the above example applications. See the [Getting Started Guide](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon) on how to use it.
1. _[Spring Framework Recipes For Real World Application Development](https://www.naturalprogrammer.com/p/spring-framework-book-of-best-practices)_ — a live book discussing key real-world topics on developing Spring applications, APIs and microservoces. Includes many Spring Lemon topics. [Click here](https://www.naturalprogrammer.com/p/spring-framework-book-of-best-practices) to get it now for FREE!
1. [Using Spring Lemon Effectively](https://github.com/naturalprogrammer/spring-lemon/wiki/Using-Spring-Lemon-Effectively)
1. [DZone Articles](https://dzone.com/users/1211183/skpatel20.html)
1. Video tutorials coming soon:
   1. Spring Framework 5 REST API Development — A Complete Blueprint For Real-World Developers
   1. Spring WebFlux Reactive REST API Development — A Complete Blueprint For Real-World Developers
   1. Microservices Using Spring Cloud — A Rapid Course For Real World Developers
   1. Join [here](https://www.naturalprogrammer.com/p/spring-framework-book-of-best-practices) to get notified and avail heavy discounts when the above courses get released

## Help and Support
1. Community help is available at [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-lemon), under the `spring-lemon` tag. Do not miss to tag the questions with `spring-lemon`!
1. [Submit an issue](https://github.com/naturalprogrammer/spring-lemon/issues) for any bug or enhancement. Please check first that the issue isn't already reported earlier.
1. Mentoring, training and professional help is provided by [naturalprogrammer.com](https://www.naturalprogrammer.com).

## Releases and Breaking Changes

1. See [here](https://github.com/naturalprogrammer/spring-lemon/releases).
