'use strict';

describe('Service: XSRFInterceptor', function () {

  // load the service's module
  beforeEach(module('angularSampleApp'));

  // instantiate service
  var XSRFInterceptor;
  beforeEach(inject(function (_XSRFInterceptor_) {
    XSRFInterceptor = _XSRFInterceptor_;
  }));

  it('should do something', function () {
    expect(!!XSRFInterceptor).toBe(true);
  });

});
