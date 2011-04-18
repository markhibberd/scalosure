goog.require('scalosure');

describe("Sample", function() {

    var sample;

    beforeEach(function() {
        sample = 'foo';
    });

    it("should solve world hunger", function() {
        expect(sample).toEqual('foo');
    });

});

