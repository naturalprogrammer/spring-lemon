'use strict';

describe('Service: ping', function () {

  // load the service's module
  beforeEach(module('angularSampleApp'));

  // instantiate service
  var ping;
  beforeEach(inject(function (_ping_) {
    ping = _ping_;
  }));

  it('should do something', function () {
    expect(!!ping).toBe(true);
  });

});
