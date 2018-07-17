'use strict';

describe('Directive: serverValidation', function () {

  // load the directive's module
  beforeEach(module('angularSampleApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<server-validation></server-validation>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the serverValidation directive');
  }));
});
