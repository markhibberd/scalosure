package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

    it("semicolons") {

        parser expect {"""

        class C {
            def m1() = this
            def m2() = this
            def m3() {
                println("m3")
            }
        }

        object o {
            def m1() = {
                val c = new C
                c.m1().m2().m3()
                println("foo")
            }
        }

        """} toDebug {"""
        
        """}
    }

}

// vim: set ts=4 sw=4 foldmethod=syntax et:
