package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("") {

        parser expect {"""

        trait T {
            def +=(n:String):Unit
        }

        class A extends T {
            val x = ""
            def +=(n:String) {}
        }

        object o {
            def m1() {
                val a = new A
                a += "bar"
            }
        }
        """} toDebug {"""

        """}
    }
}

// vim: set ts=4 sw=4 foldmethod=syntax et:
