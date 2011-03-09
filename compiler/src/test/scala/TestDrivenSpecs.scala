package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("support not operator") {

        parser expect {"""

        object o {
            def m1() = {
                val v1 = true
                val v2 = !v1
            }
        }

        """} toBe {"""

        goog.provide('o');
        o.m1 = function() {
            var self = this;
            var v1 = true;
            var v2 = !v1;
        };
        """}
    }

}

// vim: set ts=4 sw=4 foldmethod=syntax et:
