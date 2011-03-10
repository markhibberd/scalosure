package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("supports foreach method") {

        parser expect {"""

        object o {
            var xs = Map("1"->"foo", "2"->"bar")

            def m1() {
                xs foreach {
                    x => println(x._1+"="+x._2)
                }
            }
        }

        """} toBe {"""

        goog.provide('o');
        o.xs = {'1':'foo','2':'bar'};
        o.m1 = function() {
            var self = this;
            for(var _key_ in o.xs) {
                (function(x) {
                    console.log(((x._1 + '=') + x._2));
                })({_1:_key_, _2:o.xs[_key_]});
            };
        };

        """}
    }
}

// vim: set ts=4 sw=4 foldmethod=syntax et:
