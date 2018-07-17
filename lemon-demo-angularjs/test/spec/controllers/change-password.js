'use strict';

describe('Controller: ChangePasswordCtrl', function () {

  // load the controller's module
  beforeEach(module('angularSampleApp'));

  var ChangePasswordCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ChangePasswordCtrl = $controller('ChangePasswordCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
