package testing

import browser._

import goog.dom._
import goog.events
import goog.events.Event

import s2js._

import scalosure.datastore.{ Entity, Collection => Col }

object app {

  def printit(e:Entity) = e match {
      case Tenant("bar") => println("tenant: bar"); ""
      case _ => println("what"); ""
  }

  @s2js.ExportSymbol
  def start() = {
    
    val t = testing.Tenant("foo")

    val c = Col("one"->Tenant("foo"), "two"->Tenant("bar"))

    c foreach { x => printit(x._2) }

  }

  //def foo() {

    //val x = goog.json.parse("{\"name\":\"foo\"}")

    //val clickit = goog.dom.getElement("clickit")
    //val message = goog.dom.getElement("message")
    //val xio = new scalosure.net.XhrIo("http://localhost:8080/sample.json", "GET")

    //xio.onComplete = { e => 
      //val m = new HashMap(e.responseAs[JsObject[Sample]])
      //val xs = m filter { x => x._2.name.startsWith("go") } map { x => x._2 }
      //println(xs)
    //}

    //events.listen(clickit, "click", (e:Event) => { 
      //xio.send(null)
    //});
  //}

}
