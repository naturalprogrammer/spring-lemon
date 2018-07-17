'use strict';

/**
 * @ngdoc service
 * @name angularSampleApp.formService
 * @description
 * # formService
 * Factory in the angularSampleApp.
 */
angular.module('appBoot')
  .factory('formService', function($http, $location, alerts) {

    /**
     * Creates the array if not already present
     *
     * @param obj
     * @param arr
     * @returns {*}
     */
    var getFieldErrors = function(form, field) {

      if (!(field in form.serverFieldErrors)) // array not defined yet
        form.serverFieldErrors[field] = [];

      return form.serverFieldErrors[field];
    };

    var showErrors = function(form, errorData, removeObjName) {

        switch (errorData.exception) {

          case 'ConstraintViolationException': // server side JSR-303 errors
          case 'MultiErrorException': // server side form errors manually thrown

            angular.forEach(errorData.errors, function(error) { // for each fields

              var fieldName = error.field;
              if (removeObjName) // e.g. remove "user." from "user.email"
                fieldName = fieldName.substring(fieldName.indexOf(".") + 1);

              if (form[fieldName]) { // if it's a known field

                var errors = getFieldErrors(form, fieldName);

                errors.push(error.message);
                form[fieldName].$setValidity('serverError', false);

              } else { // unknown field, i.e. form level error

                // append to form level errors
                // var serverErrors = getArray(form, 'serverErrors');
                form.serverErrors.push(error.message);
              }

            });

            break;

          case 'AccessDeniedException':
            form.serverErrors = [errorData.message + '. You might need to login, logout or refresh.'];
            break;

          default:
            form.serverErrors = ['Server Error: ' + errorData.message];

        }

    };

    var formHttp = function(url, method, options) {

      if (method === 'post' || method === 'put' || method === 'patch') {

        if (options.asParam) // data should be sent as params
          return $http[method](serverUrl + url, null, {params: options.data});

        return $http[method](serverUrl + url, options.data);
      }
      // get, delete, head, jsonp
      return $http[method](serverUrl + url, {params: options.data});

    };

    var submit = function(form, url, method, options) /*formData, successMessage, form, successHandler, errorHandler)*/ {

      form.serverErrors = [];
      form.serverFieldErrors = {};

      formHttp(url, method, options)
        .success(function(data, status, headers, config) {
          if (options.successMessage) {
            alerts.setKind('success');
            alerts.addAlert(options.successMessage);
          }
          if (options.onSuccess)
            options.onSuccess(data, status, headers, config);
        })
        .error(function(data, status, headers, config) {
          showErrors(form, data, !options.asParam);
          if (options.onError)
            options.onError(data, status, headers, config);
        });
    };

    return {
        submit: submit
    };
  });
