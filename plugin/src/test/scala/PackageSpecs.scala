package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class PackageSpecs extends PrinterFixtureSpec {

    describe("classes") {

        it("can have constructors arguments") {

            parser expect {"""

                package $pkg {
                    class A()
                    class B(x:String)
                    class C(x:String, y:String)
                }

            """} toBe {"""

                goog.provide('$pkg.A');
                goog.provide('$pkg.B');
                goog.provide('$pkg.C');

                /** @constructor*/
                $pkg.A = function() {
                    var self = this;
                };

                /** @constructor*/
                $pkg.B = function(x) {
                    var self = this;
                    self.x = x;
                };
                $pkg.B.prototype.x = null;

                /** @constructor*/
                $pkg.C = function(x,y) {
                    var self = this;
                    self.x = x;
                    self.y = y;
                };
                $pkg.C.prototype.x = null;
                $pkg.C.prototype.y = null;

            """}
        }

        it("can have methods") {

            parser expect {"""

                package $pkg {
                    class A {
                        def m1() {}
                        def m2(x:String) {}
                        def m3(x:String, y:String) {}
                    }
                }

            """} toBe {"""

                goog.provide('$pkg.A');

                /** @constructor*/
                $pkg.A = function() {
                    var self = this;
                };

                $pkg.A.prototype.m1 = function() {var self = this;};
                $pkg.A.prototype.m2 = function(x) {var self = this;};
                $pkg.A.prototype.m3 = function(x,y) {var self = this;};

            """}
        }

        it("can have fields") {

            parser expect {"""

                package $pkg {
                    class A {
                        val f1 = "f1"
                        var f2 = null
                    }
                }

            """} toBe {"""

                goog.provide('$pkg.A');

                /** @constructor*/
                $pkg.A = function() {var self = this;};

                $pkg.A.prototype.f1 = 'f1';
                $pkg.A.prototype.f2 = null;

            """}
        }

        it("can inherit from another class") {

            parser expect {"""

                package $pkg {
                    class A
                    class B(var x:String) extends A
                    class C(x:String, y:String) extends B(x)
                }

            """} toBe {"""

                goog.provide('$pkg.A');
                goog.provide('$pkg.B');
                goog.provide('$pkg.C');

                /** @constructor*/
                $pkg.A = function() {var self = this;};

                /** @constructor*/
                $pkg.B = function(x) {
                    var self = this;
                    $pkg.A.call(self);
                    self.x = x;
                };
                goog.inherits($pkg.B, $pkg.A);

                $pkg.B.prototype.x = null;

                /** @constructor*/
                $pkg.C = function(x,y) {
                    var self = this;
                    $pkg.B.call(self,x);
                    self.y = y;
                };
                goog.inherits($pkg.C, $pkg.B);

                $pkg.C.prototype.x = null;
                $pkg.C.prototype.y = null;
            """}
        }

        it("can have default arguments for constructors") {

            parser expect {"""

                package $pkg {
                    class A(x:String = "")
                    class B extends A
                }

            """} toBe {"""

                goog.provide('$pkg.A');
                goog.provide('$pkg.B');

                /** @constructor*/
                $pkg.A = function(x) {
                    var self = this;
                    self.x = x;
                };

                $pkg.A.prototype.x = null;

                /** @constructor*/
                $pkg.B = function() {
                    var self = this;
                    $pkg.A.call(self);
                };
                goog.inherits($pkg.B, $pkg.A);

            """}

        }

        it("can have a constructor body") {

            parser expect {"""

                class A(x:String) {
                    val y:String = ""
                    var z:String = ""
                    z = "what"
                    println("foo"+z)
                }

            """} toBe {"""

                goog.provide('A');

                /** @constructor*/
                A = function(x) {
                    var self = this;
                    self.x = x;
                    self.z = 'what';
                    console.log(('foo' + self.z));
                };

                A.prototype.x = null;
                A.prototype.y = '';
                A.prototype.z = '';

            """}

        }
    }

    describe("objects") {

        it("can have methods") {

            parser expect {"""
                package $pkg {
                    object a {
                        def m1() {}
                    }
                }
            """} toBe {"""
                goog.provide('$pkg.a');
                $pkg.a.m1 = function() {var self = this;};
            """}
        }

        it("can have variables") {

            parser expect {"""
                package $pkg {
                    object a {
                        val x = "foo"
                    }
                }
            """} toBe {"""
                goog.provide('$pkg.a');
                $pkg.a.x = 'foo';
            """}
        }

        it("can be package objects") {

            parser expect {"""

            package p1

            package object o1 {
                val f1 = ""
                def m1() {
                    println(f1) 
                }
            }

            """} toBe {"""

            goog.provide('p1.o1');
            p1.o1.f1 = '';
            p1.o1.m1 = function() {var self = this;console.log(p1.o1.f1);};

            """}
        }
    }

    describe("misc") {

        it("println should convert to console log") {

            parser expect {"""
                object a {
                    def m1() {
                        println("f")
                    }
                }
            """} toBe {"""
                goog.provide('a');
                a.m1 = function() {var self = this;
                    console.log('f');
                };
            """}
        }

        it("export functions that have been annotated") {

            parser expect {"""
                package $pkg

                object a {
                    @s2js.ExportSymbol
                    def m1() {}
                }
            """} toBe {"""
                goog.provide('$pkg.a');
                $pkg.a.m1 = function() {var self = this;};
                goog.exportSymbol('$pkg.a.m1', $pkg.a.m1);
            """}

            
        }
    }
}

// vim: set ts=4 sw=4 foldmethod=syntax et:
