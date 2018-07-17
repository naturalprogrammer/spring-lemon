'use strict';

/**
 * @ngdoc overview
 * @name angularSampleApp
 * @description
 * # angularSampleApp
 *
 * Main module of the application.
 */

/* global variables */
var serverUrl;

(function() {

  /**
   * Computes the server url from the client url
   * @returns {*}
   */
  var computeServerUrl = function() {

    switch(window.location.hostname) {
      case 'localhost':
        return 'http://localhost:8080';
      case 'frontend.example.com':
        return 'http://api.example.com';
      default:
        throw "Unable to compute serverUrl";
    }
  };

  /**
   * https://blog.mariusschulz.com/2014/10/22/asynchronously-bootstrapping-angularjs-applications-with-server-side-data
   **/
  var bootstrap = function(appName) {

    serverUrl = computeServerUrl(); // 'http://spring-sample-app-b.cfapps.io';//

    var initInjector = angular.injector(["ng"]);

    var $http = initInjector.get("$http");
    $http.defaults.withCredentials = true;

    var config = {};
    config.headers = {"Content-Type": "application/json"};
    if (localStorage.getItem("authHeader"))
      config.headers["Authorization"] = localStorage.getItem("authHeader");

    // fetch context
    $http.get(serverUrl + '/api/core/context', config).
    success(function (data, status, headers, config) {

      data.context.user = data.user;
      localStorage.setItem("authHeader", headers('Lemon-Authorization'));
      angular.module('appBoot').constant("context", data.context);

      angular.element(document).ready(function() {
        angular.bootstrap(document, [appName]);
      });
    }).error(function (data, status, headers, config) {

      localStorage.removeItem("authHeader");
      alert("Either Authorization header expired or could not connect to server. Please try refreshing ... ");
    });
  };

  angular.module('appBoot', ['ngMessages', 'ui.bootstrap', 'vcRecaptcha'])
    .config(function ($httpProvider) {

      // needed for logging in to be remembered across requests
      $httpProvider.defaults.withCredentials = true;

      $httpProvider.interceptors.push('AuthInterceptor');

      // $httpProvider.defaults.useXDomain = true;
      // Nothing is needed.
      // $httpProvider.defaults.useXDomain = true;
      // $httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
      // delete $httpProvider.defaults.headers.post['Content-type'];
      // delete $httpProvider.defaults.headers.common['X-Requested-With'];
    });

  var myApp = angular
    .module('angularSampleApp', [
      'ngAnimate',
      'ngResource',
      'ngRoute',
      'ngSanitize',
      'ngTouch',
      'ngCookies',
      'appBoot'
    ])
    .config(function ($routeProvider, $locationProvider) {

      $locationProvider.html5Mode(true);

      $routeProvider
        .when('/', {
          templateUrl: 'views/main.html',
          controller: 'MainCtrl'
        })
        .when('/about', {
          templateUrl: 'views/about.html',
          controller: 'AboutCtrl'
        })
        .when('/signup', {
          templateUrl: 'views/signup.html',
          controller: 'SignupCtrl'
        })
        .when('/admin', {
          templateUrl: 'views/admin.html',
          controller: 'AdminCtrl'
        })
        .when('/users/:id/verification', {
          templateUrl: 'views/verify.html',
          controller: 'VerifyCtrl'
        })
        .when('/forgot-password', {
          templateUrl: 'views/forgot-password.html',
          controller: 'ForgotPasswordCtrl'
        })
        .when('/reset-password', {
          templateUrl: 'views/reset-password.html',
          controller: 'ResetPasswordCtrl'
        })
        .when('/users/:id', {
          templateUrl: 'views/profile.html',
          controller: 'ProfileCtrl'
        })
        .when('/users/:id/edit', {
          templateUrl: 'views/edit-user.html',
          controller: 'EditUserCtrl'
        })
        .when('/users/:id/change-password', {
          templateUrl: 'views/change-password.html',
          controller: 'ChangePasswordCtrl'
        })
        .when('/users/:id/request-email-change', {
          templateUrl: 'views/change-email-request.html',
          controller: 'ChangeEmailRequestCtrl'
        })
        .when('/users/:id/change-email', {
          templateUrl: 'views/change-email.html',
          controller: 'ChangeEmailCtrl'
        })
        .when('/users/:id/api-key', {
          templateUrl: 'views/api-key.html',
          controller: 'ApiKeyCtrl'
        })
        .when('/social-login-success', {
          templateUrl: 'views/social-login-success.html',
          controller: 'SocialLoginSuccessCtrl'
        })
        .otherwise({
          redirectTo: '/'
        });
    });

  bootstrap('angularSampleApp');

})();
