'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:EditUserCtrl
 * @description
 * # EditUserCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('EditUserCtrl', function ($scope, $routeParams, $location, userService, formService, authService) {

    userService.fetchById($routeParams.id, $scope, 'user');

    $scope.update = function() {

      var editedUser = $scope.user;
      editedUser.roles = [];
      if (editedUser.unverified)
        editedUser.roles.push(authService.userRoles.UNVERIFIED);
      if (editedUser.blocked)
        editedUser.roles.push(authService.userRoles.BLOCKED);
      if (editedUser.admin)
        editedUser.roles.push(authService.userRoles.ADMIN);

      var patch = [
          {"op": "replace", "path": "/name", "value": editedUser.name},
          {"op": "replace", "path": "/roles", "value": editedUser.roles}
      ];

      formService.submit($scope.form, '/api/core/users/' + $scope.user.id, 'patch', {
        data: patch,
        successMessage: 'Profile updated',
        onSuccess: function(currentUser){
          $location.url("/users/" + $routeParams.id);
          if (authService.user.id == $routeParams.id) // if the same user has logged in
            authService.user = currentUser;
        }});
    };

  });
