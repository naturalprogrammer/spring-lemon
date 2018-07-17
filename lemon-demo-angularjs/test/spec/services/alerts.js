'use strict';

describe('Service: alerts', function () {

  // load the service's module
  beforeEach(module('angularSampleApp'));

  // instantiate service
  var alerts;
  beforeEach(inject(function (_alerts_) {
    alerts = _alerts_;
  }));

  it('should do something', function () {
    expect(!!alerts).toBe(true);
  });

});
