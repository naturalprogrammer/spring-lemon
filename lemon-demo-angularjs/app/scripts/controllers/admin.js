'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:AdminCtrl
 * @description
 * # AdminCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('AdminCtrl', function ($scope, $http, $location, userService, authService, alerts) {

    $scope.permitted = function() {
      return authService.hasAnyRole(authService.userRoles.ADMIN);
    };

    $scope.email = '';

    $scope.fetchUser = function() {

      userService.fetchByEmail($scope.email, $scope.form, function(user){
        $location.url("/users/" + user.id);
      });

    };

    $scope.switchUser = function() {
      authService.switchUser($scope.email);
    };
  });
