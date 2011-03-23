package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

  it("") {

    testdriving = true

    parser expect {"""
    object o {
      def start() {
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

