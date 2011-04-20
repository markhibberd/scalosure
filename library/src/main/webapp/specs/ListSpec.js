goog.require('scalosure');
goog.require('scalosure.collection.mutable.List');

describe("lists", function() {

    var alist;

    describe("an empty list", function() {

        beforeEach(function() {
            alist = scalosure.collection.mutable.Nil;
        });

        it("should be empty by default", function() {
            expect(alist.size()).toEqual(0);
        });

    });

    describe('a list with values', function() {

        beforeEach(function() {
            alist = new scalosure.collection.mutable.List(['one', 'two', 'three']);
        });

        it('should have a size', function() {
            expect(alist.size()).toEqual(3);
        });

        it('should support foreach', function() {
            var xs = [];

            alist.foreach(function(x) {
                xs.push(x);
            });

            expect(xs.length).toEqual(3);
        });

        it('should support map', function() {

            var xs = alist.map(function(x) {
                return x+'0'
            });

            expect(xs.toArray()).toEqual(['one0', 'two0', 'three0']);
        });

        it('should support filter', function() {

            var xs = alist.filter(function(x) {
                return x === 'one';
            });

            expect(xs.toArray()).toEqual(['one'])
        });

        it('should support exists', function() {

            var rst = alist.exists(function(x) {
                return x === 'two';
            });

            expect(rst).toEqual(true);
        });

    });

});

