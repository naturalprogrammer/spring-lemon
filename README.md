# Spring Lemon

When developing **real-world** Spring REST services for JavaScript web applications, mobile clients or any consumer, you face many challenges, such as

1. How to make the API truly _stateless_, using token authentication, session sliding etc.
1. How to configure Spring Security to suit API development, e.g. returning _200_ or _401_ responses on login, configuring _CORS_, _JSON vulnerability protection_, etc.
1. How to handle _validations_ and _exceptions_ in a cross functional manner and send precise errors to the client.
1. How exactly to support multiple _social sign up/in_, using _OpenID Connect_ or _OAuth2_ providers such as _Google_ and _Facebook_.
1. How to code a robust user module (with features like _sign up_, _sign in_, _verify email_, _social sign up/in_, _update profile_, _forgot password_, _change password_, _change email_, _token authentication_ etc.) and share it across all your applications.
1. _What would be good ways to test your API_.
1. How to do _Captcha validation_.
1. How to properly organize _application properties_.
1. How to use _PATCH_ and _JsonPatch_ to handle partial updates correctly.

Coding all the above effectively needs in-depth knowledge of Spring. It also takes a lot of development time and effort, and needs to be properly maintained as new versions of Spring modules come out.

To relieve you of this non-trivial job, we thought to bring out **Spring Lemon**, a tiny open source library holding all these common configuration and components, and a production grade user module with all the abovementioned features.

Even if you don't plan to use Spring Lemon, it's a good example application to learn from, because it showcases the essential best practices for developing elegant web services using Spring.

Most Spring Boot applications can use Spring Lemon straight away, with some simple configurations. But, if you don't find it suitable for your application, feel free to fork it, or just roll out your own library by learning its patterns and practices. Better yet, be a contributor!

Watch [this video tutorial](https://www.naturalprogrammer.com/purchase?product_id=657547) or read [this quick starter guide](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon) for getting started.

## Documentation and Resources

1. Feature demo: https://youtu.be/6mNg-Feq8CY
1. _Getting started guide_
   1. [Book](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon)
   1. [Video Tutorial](https://www.naturalprogrammer.com/purchase?product_id=657547)
1. _[Example application](https://github.com/naturalprogrammer/lemon-demo)_ - A sample application using Spring Lemon. Quite similar to the one developed in the above [getting started guide](https://github.com/naturalprogrammer/spring-lemon/wiki/Getting-Started-With-Spring-Lemon), but additionally has automated tests.
1. _[API documentation](https://documenter.getpostman.com/view/305915/RVu2mqEH)_ of the above application.
1. Spring Lemon [JavaDoc](https://naturalprogrammer.github.io/javadoc/spring-lemon/1.0.0.m2/)
1. _[Example Angular 1.x front-end application](https://github.com/naturalprogrammer/lemon-demo-angular1)_ - A sample AngularJS 1.x front-end. It'll work both for the application developed in the above [getting started guide](https://documenter.getpostman.com/view/305915/lemondemo/RVnPL46k) as well as the [Lemon Demo application](https://github.com/naturalprogrammer/lemon-demo). 
1. _Mastering Real-World RESTful Web Services Development With Spring_ - Ultimate course to master REST API development using Spring. Also covers Spring Lemon in depth. A must guide if you want to become an expert Spring developer, whether you use Spring Lemon or not:
   1. **PDF eBook** - [pre-order now](https://gum.co/rIwHJ/associate) only for USD <s>115</s> 27 (plus taxes as applicable)!
   1. **Video Tutorial** - [pre-order now](https://gum.co/NPFm/associate) only for USD <s>297</s> 97 (plus taxes as applicable)!

## Help and Support
1. Community help is available at [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-lemon), under the `spring-lemon` tag. Do not miss to tag the questions with `spring-lemon`!
1. [Submit an issue](https://github.com/naturalprogrammer/spring-lemon/issues) for any bug or enhancement. Please check first that the issue isn't already reported earlier.
1. Mentoring, training and professional help is provided by [naturalprogrammer.com](http://www.naturalprogrammer.com/consulting/).

## Donate
Like Spring Lemon? We have been putting continuous efforts to develop and maintain it. If it's being useful to you, why not donate a little amount — it’ll help us give more time to the project!

[Click here](http://www.naturalprogrammer.com/support-spring-lemon/) to donate.

## Releases

1. See [here](https://github.com/naturalprogrammer/spring-lemon/releases).
