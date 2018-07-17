'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ChangeEmailRequestCtrl
 * @description
 * # ChangeEmailRequestCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('ChangeEmailRequestCtrl', function ($scope, $routeParams, $location, userService, formService) {

    userService.fetchById($routeParams.id, $scope, 'user');

    $scope.requestChange = function() {

      formService.submit($scope.form, '/api/core/users/' + $scope.user.id + '/email-change-request', 'post', {
        data: $scope.user,
        successMessage: 'A mail containing a link has been sent to the new email id. Click on that link to change your email',
        onSuccess: function(currentUser) {       	
          $location.url("/users/" + $routeParams.id);
        }});
    };

  });
