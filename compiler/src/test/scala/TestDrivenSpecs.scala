package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

  it("") {

    testdriving = true

    parser expect {"""
    object o {
      var onError:Function1[String, Unit] = (x) => {}
      def start() = {
        onError = (s) => {
          val z = s match {
            case x:String => println("was string")
            case x => println("not string")
          }
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
