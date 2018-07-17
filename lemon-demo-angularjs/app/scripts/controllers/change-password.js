'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ChangePasswordCtrl
 * @description
 * # ChangePasswordCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('ChangePasswordCtrl', function ($scope, $routeParams, $location, authService, formService) {

    $scope.passwords = {
      oldPassword: '',
      password: '',
      retypePassword: ''
    };

    $scope.changePassword = function() {
      formService.submit($scope.form, '/api/core/users/' + $routeParams.id + '/password', 'post', {
        data: $scope.passwords,
        successMessage: 'Password changed successfully',
        onSuccess: function() {
          $location.url("/");
        }
      });

    };
});
