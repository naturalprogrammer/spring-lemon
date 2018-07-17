'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ForgotPasswordCtrl
 * @description
 * # ForgotPasswordCtrl
 * Controller of the angularSampleApp
 */
angular.module('appBoot')
  .controller('ForgotPasswordCtrl', function($scope, $location, formService, abUtil) {

    abUtil.setActivePage(null);

    $scope.email = '';

    $scope.forgotPassword = function() {
      formService.submit($scope.form, '/api/core/forgot-password', 'post', {
        data: {
          email: $scope.email
        },
        asParam: true,
        successMessage: 'Please check your mail for instructions on how to reset your password.',
        onSuccess: function(data){
          $location.url("/");
      }});
    };

  });
