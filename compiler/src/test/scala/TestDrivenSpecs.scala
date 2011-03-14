package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class TestDrivenSpecs extends PrinterFixtureSpec {

  ignore("can use native javascript arrays") {

    testdriving = true

    parser expect {"""

    import s2js.JsArray

    object o {
      val name = "foo"

      def isEven(x:Any, y:Long, z:JsArray) = x.asInstanceOf[Int] % 2 == 0

      def m1() {
        val xs = JsArray(1,2,3,4)
        xs.forEach((x, y, z) => { println(name+x) })
        val mapped = xs.map((x,y,z) => { name+x })
        val filterd = xs.filter { (x,y,z) => isEven(x,y,z) }
        println(filterd)
      }
    }

    """} toDebug {"""

    goog.provide('o');
    
    o.m1 = function() {
      var self = this;
      var xs = [1,2,3];
      xs.forEach(function(x,y,z) {console.log(x);});
    };

    """}
  }
}

