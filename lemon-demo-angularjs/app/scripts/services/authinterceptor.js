'use strict';

angular.module('appBoot')
  .factory('AuthInterceptor', function ($log) {

    var AuthInterceptor = {

      request: function(config) {

        var authHeader = localStorage.getItem("authHeader");
        if (authHeader) {
          config.headers['Authorization'] = authHeader;
          $log.info("Authorization being sent to server: " + authHeader);
        }

        return config;
      },

      response: function(response) {

        var authHeader = response.headers('Lemon-Authorization');

        if (authHeader) {

          localStorage.setItem("authHeader", authHeader);
          $log.info("Lemon-Authorization received: " + authHeader);

          // if (response.data.username) {
          //
          //   var initInjector = angular.injector(["ng"]);
          //   var authService = initInjector.get("authService");
          //   authService.changeUser(data);
          // }
        }

        return response;
      }
    };

    return AuthInterceptor;
  });
