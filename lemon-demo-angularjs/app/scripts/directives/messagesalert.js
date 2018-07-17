'use strict';

/**
 * @ngdoc directive
 * @name angularSampleApp.directive:messagesAlert
 * @description
 * # messagesAlert
 */
angular.module('appBoot')
  .directive('messagesAlert', function() {
      return {
    	  restrict: 'E',
    	  scope: {
    		  messages: '=',
          kind: '@'
        },
        controller: function($scope) {
          $scope.clear = function () {
            $scope.messages.length = 0;
            return false;
          };
        },
      templateUrl: 'views/directives/messages-alert.html'
      };
    });
