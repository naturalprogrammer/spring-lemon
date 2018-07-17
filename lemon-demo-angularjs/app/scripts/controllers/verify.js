'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:VerifyCtrl
 * @description
 * # VerifyCtrl
 * Controller of the angularSampleApp
 */
angular.module('appBoot')
  .controller('VerifyCtrl', function ($scope, $http, $routeParams, $location, authService, alerts) {

    $scope.verify = function() {
      $http.post(serverUrl + '/api/core/users/' + $routeParams.id + '/verification', $.param({
    	      code: $routeParams.code
          }), {
          headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .success(function(data, status, headers, config) {

          alerts.setKind('success');
          alerts.addAlert('Verification successful.');
          authService.changeUser(data)

        })
        .error(function(data, status, headers, config) {

          alerts.setKind('danger');
          alerts.addAlert('Verification failed: ' + data.message);

        });
      $location.url("/");
    };
  });
