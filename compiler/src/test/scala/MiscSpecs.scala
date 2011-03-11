package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class MiscSpecs extends PrinterFixtureSpec {

    it("support implicit conversions") {

        parser expect {"""

        class B(name:String) {
            def doit() {}
        }

        object o {

            implicit def string2b(a:String):B = new B(a)

            def m1() {
                val a = "foo"
                a.doit()
            }
        }

        """} toBe {"""

        goog.provide('B');
        goog.provide('o');
        
        /** @constructor*/B = function(name) {
            var self = this;
            self.name = name;
        };

        B.prototype.name = null;
        B.prototype.doit = function() {
            var self = this;
        };
        
        o.string2b = function(a) {
            var self = this;
            return new B(a);
        };
        
        o.m1 = function() {
            var self = this;
            var a = 'foo';
            o.string2b(a).doit();
        };

        """}
    }
}
