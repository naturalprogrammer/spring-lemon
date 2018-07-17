'use strict';

describe('Service: domain', function () {

  // load the service's module
  beforeEach(module('angularSampleApp'));

  // instantiate service
  var domain;
  beforeEach(inject(function (_domain_) {
    domain = _domain_;
  }));

  it('should do something', function () {
    expect(!!domain).toBe(true);
  });

});
