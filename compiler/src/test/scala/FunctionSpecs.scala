package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class FunctionSpecs extends PrinterFixtureSpec {

    describe("functions") {

      it("can be higher-orderd") {

        parser expect {"""

        object o {

          def toUpper(x:String) = x.toUpperCase

          def m1(fn:(String) => String) {
            val x = fn("From M1") 
            println(x)
          }

          def m2() {
            m1 { toUpper }
            m1 { (x) => toUpper(x) }
          }
        }

        """} toBe {"""

        goog.provide('o');
        o.toUpper = function(x) {
          var self = this;
          return x.toUpperCase();
        };
        o.m1 = function(fn) {
          var self = this;
          var x = fn('From M1');
          console.log(x);
        };
        o.m2 = function() {
          var self = this;
          o.m1(function(x) {
              return o.toUpper(x);
            });
          o.m1(function(x) {
              return o.toUpper(x);
            });
        };

        """}
      }

      it("can have a return value") {

        parser expect {"""

        object a {
          def m1() = {
            val x = "foo"
            x + "bar"
          }
          def m2() = {
            "foo"
          }
          def m3() = {
            "foo"+"bar"
          }
          def m4() {
            "foo"+"bar"
          }
        }

        """} toBe {"""
        goog.provide('a');
        a.m1 = function() {
          var self = this;
          var x = 'foo';
          return (x + 'bar');
        };
        a.m2 = function() {
          var self = this;
          return 'foo';
        };
        a.m3 = function() {
          var self = this;
          return ('foo' + 'bar');
        };
        a.m4 = function() {
          var self = this;
          ('foo' + 'bar');
        };
        """}
      }

      it("can have arguments") {

        parser expect {"""
        object a {
          def m1(x:String) {}
          def m2(x:String, y:Int) {}
        }
        """} toBe {"""
        goog.provide('a');
        a.m1 = function(x) {var self = this;};
        a.m2 = function(x,y) {var self = this;};
        """}

      }
    }

  describe("class functions") {

    it("override base class functions") {

      parser expect {"""
        package $pkg
        class a {
          def m1() {}
          def m2(x:String) {}
        }
        class b extends a {
          override def m1() {super.m1()}
          override def m2() {super.m2("foo")}
        }
        """} toBe {"""
        goog.provide('$pkg.a');
        goog.provide('$pkg.b');

        /** @constructor*/
        $pkg.a = function() {var self = this;};

        $pkg.a.prototype.m1 = function() {var self = this;};
        $pkg.a.prototype.m2 = function(x) {var self = this;};

        /** @constructor*/
        $pkg.b = function() {
          var self = this;
          $pkg.a.call(self);
        };
        goog.inherits($pkg.b, $pkg.a);

        $pkg.b.prototype.m1 = function() {var self = this;$pkg.b.superClass_.m1.call(self);};
        $pkg.b.prototype.m2 = function() {var self = this;$pkg.b.superClass_.m2.call(self,'foo');};
        """}

    }
  }

  describe("anon functions") {

    it("can be assigned to variables") {

      parser expect {"""
      object a {
        val x = (y:String) => { println(y) }
      }
      """} toBe {"""
      goog.provide('a');
      a.x = function(y) {return console.log(y);};
      """}
    }

    it("can have multiple statements") {

      parser expect {"""
      object a {
        val x = (y:String) => {
          println("what")
          println(y) 
        }
      }
      """} toBe {"""
      goog.provide('a');
      a.x = function(y) {
        console.log('what');
        console.log(y);
      };
      """}
    }
  }
}
