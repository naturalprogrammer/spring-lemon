'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ProfileCtrl
 * @description
 * # ProfileCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('ProfileCtrl', function ($scope, $routeParams, userService) {

    userService.fetchById($routeParams.id, $scope, 'user');

    $scope.resendVerificationMail = function() {
      userService.resendVerificationMail();
    };

    //$scope.verify = function() {
    //  authService.resendVerificationMail();
    //};
    //
    //$scope.authService = authService;

  });
