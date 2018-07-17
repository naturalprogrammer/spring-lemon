'use strict';

describe('Controller: ChangeEmailCtrl', function () {

  // load the controller's module
  beforeEach(module('angularSampleApp'));

  var ChangeEmailCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ChangeEmailCtrl = $controller('ChangeEmailCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
