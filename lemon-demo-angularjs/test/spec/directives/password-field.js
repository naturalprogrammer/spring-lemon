'use strict';

describe('Directive: passwordField', function () {

  // load the directive's module
  beforeEach(module('angularSampleApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<password-field></password-field>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the passwordField directive');
  }));
});
