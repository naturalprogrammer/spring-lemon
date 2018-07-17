'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:SignupCtrl
 * @description
 * # SignupCtrl
 * Controller of the angularSampleApp
 */
angular.module('appBoot')
  .controller('SignupCtrl', function($scope, $location, formService, vcRecaptchaService, abUtil, authService) {

    abUtil.setActivePage('SIGNUP');

    $scope.user = {
    		email: '',
    		name: '',
    		password: '',
        captchaResponse: ''
    };

    $scope.agree = false;

		$scope.signup = function() {
	      formService.submit($scope.form, '/api/core/users', 'post', {
	        data: $scope.user,
	        successMessage: 'Signed in. Please check your mail for validation email',
	        onSuccess: function(data){
	          $location.url("/");
	          authService.user = data;
	        },
	        onError: function(){
	          vcRecaptchaService.reload($scope.widgetId);
      }});
    };

    // reCaptcha
    $scope.widgetId = null;

    $scope.setResponse = function (response) {
      console.info('Response available');
      $scope.user.captchaResponse = response; //"invalid response for testing";
    };
    $scope.setWidgetId = function (widgetId) {
      console.info('Created widget ID: %s', widgetId);
      $scope.widgetId = widgetId;
    };

  });
