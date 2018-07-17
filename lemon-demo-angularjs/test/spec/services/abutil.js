'use strict';

describe('Service: abUtil', function () {

  // load the service's module
  beforeEach(module('angularSampleApp'));

  // instantiate service
  var abUtil;
  beforeEach(inject(function (_abUtil_) {
    abUtil = _abUtil_;
  }));

  it('should do something', function () {
    expect(!!abUtil).toBe(true);
  });

});
