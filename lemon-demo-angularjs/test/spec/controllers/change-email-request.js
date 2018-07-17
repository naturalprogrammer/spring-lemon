'use strict';

describe('Controller: ChangeEmailRequestCtrl', function () {

  // load the controller's module
  beforeEach(module('angularSampleApp'));

  var ChangeEmailRequestCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ChangeEmailRequestCtrl = $controller('ChangeEmailRequestCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
