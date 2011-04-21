goog.require('scalosure');
goog.require('scalosure.datastore.manager');

describe('datastore', function() {

    var dsm;

    describe('registered collections', function() {

        beforeEach(function() {

            dsm = scalosure.datastore.manager;
            dsm.async = false;

            dsm.register([
                {name:'foo', url:'http://localhost:8080/foo.json'},
                {name:'foo', url:'http://localhost:8080/bar.json'}
            ], function() {});

        });

        it('should have all registered collections', function() {
            expect(dsm.cols['foo']).toNotBe(null)
            expect(dsm.cols['bar']).toNotBe(null)
        });

        it('should have a collection accessor', function() {
            expect(dsm.get('foo')).toNotEqual(null);
        });

    });

    describe('ad-hoc quering', function() {

        it('should execute a given query', function() {

        });
    });

});

