package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class ArraySpecs extends PrinterFixtureSpec {

    describe("arrays") {

        it("support standard javascript array functions") {

            testdriving = true

            parser expect {"""

            import s2js.JsArray

            object o {
                val name = "foo"

                def isEven(x:Any, y:Long, z:JsArray) = x.asInstanceOf[Int] % 2 == 0

                def start() {
                    val xs = JsArray(1,3,5)
                    xs.forEach((x, y, z) => { println(name+x) })
                    val mapped = xs.map((x,y,z) => { name+x })
                    val filterd = xs.filter { isEven }
                    val alleven = xs.every { isEven }
                    val anyeven = xs.some { isEven }
                    println(anyeven)
                }
            }

            """} toBe {"""
            goog.provide('o');
            o.name = 'foo';
            o.isEven = function(x,y,z) {
                var self = this;
                return ((x % 2) == 0);
            };
            o.start = function() {
                var self = this;
                var xs = [1,3,5];
                xs.forEach(function(x,y,z) {
                        console.log((o.name + x));
                    });
                var mapped = xs.map(function(x,y,z) {
                        return (o.name + x);
                    });
                var filterd = xs.filter(function(x,y,z) {
                        return o.isEven(x,y,z);
                    });
                var alleven = xs.every(function(x,y,z) {
                        return o.isEven(x,y,z);
                    });
                var anyeven = xs.some(function(x,y,z) {
                        return o.isEven(x,y,z);
                    });
                console.log(anyeven);
            };
            """}
        }
        it("can be iterated using while statement") {

            parser expect {"""

            object o {
                def m1() = {
                    val xs = Array("one", "two", "three")
                    var i = 0
                    while(i < xs.length) {
                        println(xs(i))
                        i = i + 1
                    }
                }
            }

            """} toBe {"""

            goog.provide('o');
            goog.require('goog.array');

            o.m1 = function() {
                var self = this;
                var xs = ['one','two','three'];
                var i = 0;
                while((i < xs.length)) {
                    console.log(xs[i]);
                    i = (i + 1);
                };
            };

            """}
        }

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
