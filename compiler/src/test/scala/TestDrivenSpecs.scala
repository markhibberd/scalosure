package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

  it("") {

    testdriving = true

    parser expect {"""
    import scalosure.JsArray

    class F(xs:JsArray[Any]) {
      def fe() {
        var i = 0
        while(i < xs.length) {
            println(xs(i))
            i = i + 1
        }
      }
    }

    object o {
      def start() = {
        val zs = JsArray.empty[String]
        zs.push("foo")
        println(zs(0))
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
