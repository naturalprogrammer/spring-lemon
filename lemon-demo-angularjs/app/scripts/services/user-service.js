'use strict';

/**
 * @ngdoc service
 * @name angularSampleApp.userService
 * @description
 * # userService
 * Factory in the angularSampleApp.
 */
angular.module('angularSampleApp')
  .factory('userService', function ($http, formService, authService, alerts) {

    var User = function(rawUser) {

      this.id = rawUser.id;
      this.name = rawUser.name;
      this.email = rawUser.email;
      this.roles = rawUser.roles;
      this.version = rawUser.version;

      this.unverified = this.hasRole(authService.userRoles.UNVERIFIED);
      this.blocked = this.hasRole(authService.userRoles.BLOCKED);
      this.admin = this.hasRole(authService.userRoles.ADMIN);
      this.goodUser = !(this.unverified || this.blocked);
      this.goodAdmin = this.admin && this.goodUser;

      this.editable = authService.isAuthenticated() && (this.id == authService.user.id || authService.isGoodAdmin());
      this.rolesEditable = authService.isGoodAdmin() && this.id != authService.user.id;
    };

    User.prototype.hasRole = function(role) {
      return this.roles.indexOf(role) !== -1;
    }

    User.prototype.rolesStr = function() {
      if (this.roles.length === 0)
        return "No special roles";
      return this.roles.join(', ');
    };

    User.prototype.editLink = function() {
      return '/users/' + this.id + '/edit';
    };

    User.prototype.changePasswordLink = function() {
      return '/users/' + this.id + '/change-password';
    };

    User.prototype.changeEmailLink = function() {
      return '/users/' + this.id + '/request-email-change';
    };

    User.prototype.apiKeyLink = function() {
      return '/users/' + this.id + '/api-key';
    };

    // Public API here
    return {

      fetchById: function (id, into, as) {

        into[as] = null;

        $http.get(serverUrl + '/api/core/users/' + id)
          .success(function(rawUser, status, headers, config) {
            into[as] = new User(rawUser);
          })
          .error(function(data, status, headers, config) {
            alerts.setKind("danger");
            alerts.addAlert(data.message);
          });

      },

      fetchByEmail: function(email, form, onSuccess) {

        formService.submit(form, '/api/core/users/fetch-by-email', 'post', {
          data: {
            email: email
          },
          asParam: true,
          onSuccess: onSuccess
        });

      },

      resendVerificationMail: function(id) {

        if (!id)
          id = authService.user.id;

        $http
          .post(serverUrl + '/api/core/users/' + id + '/resend-verification-mail')
          .then(function () {
            alerts.addAlert("Mail sent. ");
          }, function (data) {
            alerts.setKind("danger");
            alerts.addAlert("Couldn't send mail: " + data.data.message);
          });
      }
    };
  });
