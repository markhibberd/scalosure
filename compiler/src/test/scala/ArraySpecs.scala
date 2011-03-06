package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class ArraySpecs extends PrinterFixtureSpec {

    describe("arrays") {

        it("can have strings") {

            parser expect {"""
                object a {
                    def m1() {
                        val xs = Array("one", "two")
                    }
                }
            """} toBe {"""
                goog.provide('a');
                goog.require('goog.array');
                a.m1 = function() {
                    var self = this;
                    var xs = ['one','two'];
                };
            """}
        }

        ignore("can have numbers") {

            parser expect {"""
                object a {
                    def m1() {
                        val xs = Array(1, 2)
                    }
                }
            """} toBe {"""
                goog.provide('a');
                goog.require('goog.array');
                a.m1 = function() {
                    var self = this;
                    var xs = [1,2];
                };
            """}
        }

        it("can have function call elements") {

            parser expect {"""

            object o1 {
                def m1() = "foo"
                def m2() = "bar"
                val v1 = Array(m1, m2)
            }

            """} toBe {"""

            goog.provide('o1');
            goog.require('goog.array');
            o1.m1 = function() {
                var self = this;
                return 'foo';
            };
            o1.m2 = function() {
                var self = this;
                return 'bar';
            };
            o1.v1 = [o1.m1(),o1.m2()];

            """}
        }
    }
}

// vim: set ts=4 sw=4 et:
