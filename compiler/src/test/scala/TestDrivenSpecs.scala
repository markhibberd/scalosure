package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("supports foreach method") {

        parser expect {"""

        case class A(name:String)

        object o {
            def m1() {
                val a = A("name")
            }
        }

        """} toBe {"""

        goog.provide('A');
        goog.provide('o');

        /** @constructor*/
        A = function(name) {
            var self = this;
            self.name = name;
        };
        
        A.prototype.name = null;

        o.m1 = function() {
            var self = this;
            var a = new A('name');
        };

        """}
    }
}

// vim: set ts=4 sw=4 foldmethod=syntax et:
