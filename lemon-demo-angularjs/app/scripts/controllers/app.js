'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:AppCtrl
 * @description
 * # AppCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('AppCtrl', function($scope, alerts, authService, userService, context, abUtil) {
    $scope.alerts = alerts;
    $scope.authService = authService;
    $scope.context = context;
    $scope.abUtil = abUtil;

    $scope.unverified = function() {
      return authService.hasAnyRole(authService.userRoles.UNVERIFIED);
    };

    $scope.resendVerificationMail = function() {
      userService.resendVerificationMail();
    }

    //$scope.$watch(authService.user, function(){
    //  $scope.unverified = authService.hasAnyRole(authService.userRoles.UNVERIFIED);
    //});

  });
