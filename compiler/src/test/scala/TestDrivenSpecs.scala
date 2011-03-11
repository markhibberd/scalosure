package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("can have multiple arguments lists") {

        parser expect {"""

        object o1 {

            def m1(name:String)(fn:(String) => Unit) {
                fn(name)
            }

            def m3() {

                m1("foo") {
                    x => println(x)
                }
            }
        }

        """} toDebug {"""

        goog.provide('o1');

        o1.m1 = function(name,fn) {
            var self = this;
            fn(name);
        };

        o1.m3 = function() {
            var self = this;
            o1.m1('foo',function(x) {console.log(x);});
        };

        """}
    }
}

// vim: set ts=4 sw=4 foldmethod=syntax et:
