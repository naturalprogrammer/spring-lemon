'use strict';

/**
 * @ngdoc service
 * @name angularSampleApp.alerts
 * @description
 * # alerts
 * Factory in the angularSampleApp.
 */
angular.module('appBoot')
  .factory('alerts', function($sce) {

    var alerts = [];
    var alertKind = "success";

    return {

      getKind: function() {
        return alertKind;
      },

      setKind: function(kind) {
        alertKind = kind;
      },

      getAlerts: function() {
        return alerts;
      },

      addAlert: function(alert) {
        alerts.push(alert);
      },

      addTrustedAlert: function(alert) {
        alerts.push($sce.trustAsHtml(alert));
      },

    	clear: function() {
    		this.alerts.length = 0;
    	}
    };

  });
