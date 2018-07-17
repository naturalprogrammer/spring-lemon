'use strict';

describe('Controller: ResetPasswordCtrl', function () {

  // load the controller's module
  beforeEach(module('angularSampleApp'));

  var ResetPasswordCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ResetPasswordCtrl = $controller('ResetPasswordCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
