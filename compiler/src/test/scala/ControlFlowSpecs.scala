package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class ControlFlowSpecs extends PrinterFixtureSpec {

    it("can have while loops") {

        parser expect {"""
            object a {
                def m1() {
                    var x = 0
                    while(x < 10) {
                        x = x + 1
                        println(x)
                    }
                }
            }
        """} toBe {"""
            goog.provide('a');
            a.m1 = function() {
                var self = this;
                var x = 0;
                while((x < 10)) {
                    x = (x + 1);
                    console.log(x);
                };
            };
        """}
    }

    ignore("foreach") {

        it("delegates to closure foreach support") {

            parser expect {"""
                object a {
                    def m1() {
                        val xs = Array("one", "two")
                        xs.foreach {
                            x => println(x)
                        }
                    }
                }
            """} toBe {"""
                goog.provide('a');
                goog.require('goog.array');
                a.m1 = function() {
                    var self = this;
                    var xs = ['one','two'];
                    goog.array.forEach(xs, function(x) {
                        console.log(x);
                    }, self);
                };
            """}
        }
    }

    ignore("for statements") {

        it("can iterate arrays") {

            parser expect {"""

                object a {
                    def m1() = {
                        for(x <- 0 to 2) {
                            println("foo"+x)
                        }
                    }
                }

            """} toBe {"""
                goog.provide('a');
                a.m1 = function(x) {
                    var self = this;
                };
            """}
        }

    }

    describe("if statements") {

        it("can have assignments") {

            parser expect {"""

            object o1 {
                def m1() {
                    var x = ""
                    if(x == "") {
                        x = "default" 
                    } else {
                        println("what")
                    }
                }
            }

            """} toBe {"""

            goog.provide('o1');
            o1.m1 = function() {
                var self = this;
                var x = '';
                (x == '') ? function() {x = 'default';}() : function() {console.log('what');}();
            };

            """}
        }

        it("can have return values") {

            parser expect {"""

            object o1 {
                def m1():String = "fooy"
                def m2(x:String) {
                    val y = if(x == "foo") {
                        println("was foo")
                    } else {
                        println("was not")
                        m1
                    }
                }
            }

            """} toBe {"""

            goog.provide('o1');
            o1.m1 = function() {var self = this;return 'fooy';};
            o1.m2 = function(x) {
                var self = this;
                var y = (x == 'foo') ? function() {console.log('was foo');}() : function() {console.log('was not');return o1.m1();}();
            };

            """}
        }

        ignore("can have else if statements") {

        }
    }

    describe("match statements") {

        it("can be return a value") {

            parser expect {"""

            object o1 {
                def m1(x:String) {
                    x match {
                        case "0" => println("zero")
                        case "1" => println("one")
                        case _ => println("none")
                    }
                }
            }

            """} toBe {"""

            goog.provide('o1');
            o1.m1 = function(x) {
                var self = this;
                switch(x) {
                    case '0': console.log('zero');break;
                    case '1': console.log('one');break;
                    default: console.log('none');break;
                };
            };

            """}
        }

        it("can be side effecting") {

            parser expect {"""

            object o1 {
                def m1(x:String) {
                    val y = x match {
                        case "0" => println("zero")
                        case "1" => println("one")
                        case _ => println("none")
                    }
                }
            }

            """} toBe {"""

            goog.provide('o1');
            o1.m1 = function(x) {
                var self = this;
                var y = function() {
                    switch(x) {
                        case '0': return console.log('zero');
                        case '1': return console.log('one');
                        default: return console.log('none');
                    }
                }();
            };

            """}
        }
    }
}

// vim: set ts=4 sw=4 et:
