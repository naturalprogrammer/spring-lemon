'use strict';

/**
 * @ngdoc service
 * @name angularSampleApp.authService
 * @description
 * # authService
 * Service in the angularSampleApp.
 */
angular.module('appBoot')
  .factory('authService', function ($http, $modal, context, alerts) {

    var authService = {

      userRoles: {
        ADMIN: 'ADMIN',
        //USER: 'USER',
        UNVERIFIED: 'UNVERIFIED',
        BLOCKED: 'BLOCKED'
      },

      user: null,

      switchUserCount: 0

    };

    authService.showLoginDialog = function() {

      $modal.loginModalInstance = $modal.open({
        templateUrl: 'views/login-dialog.html',
        controller: 'LoginCtrl'
      });

    };

    authService.isAuthenticated = function () {
      return authService.user != null;
    };

    authService.hasAnyRole = function (roles) {

      if (!authService.isAuthenticated())
        return false;

      //if (roles === "*") // any role
      //  return true;
      //
      if (!angular.isArray(roles))
        roles = roles.split(",");

      for (var i = 0; i < roles.length; i++)

        if (authService.user.roles.indexOf(roles[i]) !== -1)
          return true;

      return false;
    };

    authService.logout = function() {

      localStorage.removeItem("authHeader");
      authService.changeUser(null);

      // $http
      //   .post(serverUrl + '/logout')
      //   .then(function (data) {
      //     authService.changeUser(null);
      //   }, function (data) {
      //     alerts.addAlert("Couldn't logout: " + data.data.message);
      //   });
    };

    authService.changeUser = function(user) {
      authService.user = user;
      // ping();
      //if (authService.hasRole(authService.userRoles.UNVERIFIED))
      //  alerts.addTrustedAlert("Your email is UNVERIFIED. " +
      //      "<a href='#' ng-click='authService.resendVerificationMail()'>" +
      //      "Resend verification mail</a>.");
    };

    authService.hasRole = function(role) {
      return authService.isAuthenticated() && authService.user.roles.indexOf(role) !== -1;
    };

    authService.isGoodUser = function() {
      return !(authService.hasRole(authService.userRoles.UNVERIFIED) ||
               authService.hasRole(authService.userRoles.BLOCKED));
    };

    authService.isGoodAdmin = function() {
      return authService.hasRole(authService.userRoles.ADMIN) && authService.isGoodUser();
    };

    /* no more used, we are stateless now!
    authService.switchUser = function(email) {

      $http.get(
        serverUrl + '/login/impersonate',
        {params: {
          username: email
        }}).
        success(function (data, status, headers, config) {
          authService.switchUserCount++;
          authService.changeUser(data);
          alerts.addAlert("Switched user");
        })
        .error(function (data, status, headers, config) {
          alerts.addAlert("Could not switch user");
        });

    };

    authService.switchUserBack = function() {

      $http.get(
        serverUrl + '/logout/impersonate')
        .success(function (data, status, headers, config) {
          authService.switchUserCount--;
          authService.changeUser(data);
          alerts.addAlert("Switched user back");
        })
        .error(function (data, status, headers, config) {
          alerts.addAlert("Could not switch user");
        });

    };
	*/
    authService.changeUser(context.user);

    return authService;

  });
