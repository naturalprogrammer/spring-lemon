'use strict';

/**
 * @ngdoc service
 * @name angularSampleApp.abUtil
 * @description
 * # abUtil
 * Factory in the angularSampleApp.
 */
angular.module('appBoot')
  .factory('abUtil', function () {

    return {

      //reCaptchaSiteKey: null,

      activePage: null,

      setActivePage: function (page) {
        this.activePage = page;
      },

      isActive: function(page) {
        return this.activePage == page;
      }
    };

  });
