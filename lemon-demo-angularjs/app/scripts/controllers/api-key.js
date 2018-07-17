'use strict';

/**
 * @ngdoc function
 * @name angularSampleApp.controller:ApiKeyCtrl
 * @description
 * # ProfileCtrl
 * Controller of the angularSampleApp
 */
angular.module('angularSampleApp')
  .controller('ApiKeyCtrl', function ($scope, $routeParams, $http, userService, alerts) {

    userService.fetchById($routeParams.id, $scope, 'user');

    $scope.permitted = function() {
      return $scope.user && $scope.user.editable;
    };

    $scope.token = null;

    $scope.createApiKey = function() {
        $http.post(serverUrl + '/api/core/fetch-new-auth-token',
        	$.param({'username': $scope.user.email, 'expirationMillis': 3155695200000}), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            })
          .success(function (apiKeyData, status, headers, config) {
            $scope.apiKey = apiKeyData.token;
          })
          .error(function (error, status, headers, config) {
            alerts.setKind('danger');
            alerts.addAlert("Error creating API key: " + error.message);
          });
    }
  });
