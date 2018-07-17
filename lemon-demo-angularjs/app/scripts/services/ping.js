// 'use strict';
//
// /**
//  * @ngdoc service
//  * @name angularSampleApp.ping
//  * @description
//  * # ping
//  * Factory in the angularSampleApp.
//  */
// angular.module('appBoot')
//   .factory('ping', function ($http, $log, alerts) {
//
//     return function() {
//
//       $http.get(serverUrl + '/api/core/ping')
//         .success(function(){
//           $log.info("Ping completed");
//         })
//         .error(function (data, status, headers, config) {
//           alerts.addAlert("Could not ping server. Please try refreshing after sometime");
//         });
//     };
//   });
