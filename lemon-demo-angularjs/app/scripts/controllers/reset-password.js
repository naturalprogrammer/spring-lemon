'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ResetPasswordCtrl
 * @description
 * # ResetPasswordCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('ResetPasswordCtrl', function ($scope, $routeParams, $location, formService) {

    $scope.newPassword = '';
    $scope.retypePassword = '';

    $scope.resetPassword = function() {
      formService.submit($scope.form, '/api/core/reset-password/', 'post', {
        data: {
          newPassword: $scope.newPassword,
          code: $routeParams.code
        },
        //asParam: true,
        successMessage: 'Password changed successfully',
        onSuccess: function() {
          $location.url("/");
        }
      });

    };
  });
