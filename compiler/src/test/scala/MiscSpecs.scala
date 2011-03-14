package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class MiscSpecs extends PrinterFixtureSpec {

  ignore("can use native javascript arrays") {

    parser expect {"""

    import s2js.JsArray

    object o {
      def m1() {
        val xs = JsArray("one", "two")
        xs.forEach((x, y, z) => {
          println(x)
        })
      }
    }

    """} toBe {"""
    goog.provide('o');
    goog.require('s2js.JsArray');
    
    o.m1 = function() {
      var self = this;
      var xs = ['one','two'];
      xs.forEach(function(x,y,z) {console.log(x);});
    };

    """}
  }

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

    """} toBe {"""

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
