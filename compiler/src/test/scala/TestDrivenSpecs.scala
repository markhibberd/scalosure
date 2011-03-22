package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

  it("") {

    testdriving = true

    parser expect {"""

    object o {

      def foo(v1a:String, v1b:String = "foo")(v2: => Unit) {}

      def start() {
        foo("1a") {
          println("foo")
        }
      }
    }
    """} toDebug {"""
    goog.provide('o');
    o.start = function() {
      var self = this;
    };
    """}
  }
}

