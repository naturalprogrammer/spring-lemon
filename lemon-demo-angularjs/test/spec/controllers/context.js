'use strict';

describe('Controller: ContextCtrl', function () {

  // load the controller's module
  beforeEach(module('angularSampleApp'));

  var ContextCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ContextCtrl = $controller('ContextCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
