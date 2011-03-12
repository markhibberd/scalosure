package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("") {

        parser expect {"""

        import scala.collection.mutable.HashMap

        object o1 {
            def m1() {

                val m = Map("one"->"foo")
            }
        }

        """} toDebug {"""

        """}
    }
}

// vim: set ts=4 sw=4 foldmethod=syntax et:
