goog.require('scalosure');
goog.require('scalosure.Some');

describe('the option monad', function() {

    var value;

    describe('given a value', function() {

        beforeEach(function() {
            value = scalosure.Some.$apply('foo');
        });

        it("should not be empty", function() {
            expect(value.isEmpty()).toEqual(false);
        });

        it('should directly provide the value', function() {
            expect(value.get()).toEqual('foo');
        });

        it('should provide the value instead of default', function() {
            expect(value.getOrElse('bar')).toEqual('foo');
        });

        it('should be mapable to a new value', function() {
            expect(value.map(function(x) { return x+'1'; }).get()).toEqual('foo1')
        });

    });

    describe('given no value', function() {

        beforeEach(function() {
            value = new scalosure.None();
        });

        it("should be empty", function() {
            expect(value.isEmpty()).toEqual(true);
        });

        it('should provide null as a value', function() {
            expect(value.get()).toEqual(null);
        });

        it('should provide a default value', function() {
            expect(value.getOrElse('bar')).toEqual('bar');
        });
    });

});

