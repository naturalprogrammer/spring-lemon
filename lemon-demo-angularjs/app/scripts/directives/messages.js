'use strict';

/**
 * @ngdoc directive
 * @name angularSampleApp.directive:messages
 * @description
 * # messages
 */
angular.module('appBoot')
    .directive('messages', function($compile) {
      return {
    	  restrict: 'E',
    	  scope: {
    		 messages: '=messages'
        },
        //compile: function() {
        //  return {
        //    post: function (scope, element, attributes) {
        //      scope.$watch(function(){
        //        return scope.messages.length;
        //      }, function () {
        //        $compile(element.contents())(scope);
        //      });
        //    }
        //  };
        //},
        //link: function(scope, element, attr) {
        //  scope.$watch(function(){
        //    return scope.messages.length;
        //  }, function () {
        //    $compile(element.contents())(scope);
        //  });
        //},
        templateUrl: 'views/directives/messages.html'
      };
    });
