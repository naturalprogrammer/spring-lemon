'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ChangeEmailCtrl
 * @description
 * # ChangeEmailCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('ChangeEmailCtrl', function ($scope, $http, $routeParams, $location, authService, alerts) {

    $scope.permitted = function () {
      return authService.isAuthenticated();
    };

    $scope.changeEmail = function() {
      $http.post(serverUrl + '/api/core/users/' + $routeParams.id + '/email', $.param({
        code: $routeParams.code
      }), {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }).success(function(data, status, headers, config) {

          alerts.setKind('success');
          alerts.addAlert('Email changed.');
          authService.changeUser(data);
        })
        .error(function(data, status, headers, config) {

          alerts.setKind('danger');
          alerts.addAlert('Changing email failed: ' + data.message);
        });
      $location.url("/");
    };
  });
