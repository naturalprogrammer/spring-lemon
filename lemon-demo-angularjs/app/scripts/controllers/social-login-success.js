'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('SocialLoginSuccessCtrl', function ($scope, $window, $routeParams) {

    $window.opener.socialLoginSuccess($routeParams.token);
    $window.close();
  });
