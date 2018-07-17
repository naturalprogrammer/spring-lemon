'use strict';

/**
 * @ngdoc directive
 * @name appBoot.directive:secured
 * @description
 * # secured
 */
angular.module('appBoot')
  .directive('secured', function () {
    return {
      templateUrl: 'views/directives/secured.html',
      restrict: 'E',
      transclude: true,
      scope: {
        permitted: '='
      }
      ,controller: function($scope, authService) {
        $scope.authService = authService;
        //$scope.permitted = authService.hasAnyRole(roles);
      }
    };
  });
