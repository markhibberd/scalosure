package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

  ignore("1") {

    testdriving = true

    parser expect {"""

    """} toDebug {"""

    """}

  }
}
