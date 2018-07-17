'use strict';

/**
 * @ngdoc directive
 * @name angularSampleApp.directive:serverValidation
 * @description
 * # serverValidation
 */
angular.module('appBoot')
  .directive('serverValidation', function() {
      return {
    	  restrict: 'A',
    	  require: 'ngModel',
        link: function(scope, element, attrs, ngModel) {
          // When the user changes the input,
          // reset the validity to true
          // See also http://stackoverflow.com/questions/7105997#7106392
          element.on('input', function() {
            scope.$apply(function(){
                ngModel.serverErrors = [];
                ngModel.$setValidity('serverError', true);
            });
          });
        }
      };
    });
