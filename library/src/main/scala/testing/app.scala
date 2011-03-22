package testing

import goog.dom._
import goog.events

import s2js._
import scalosure.collection._

object app {

  @s2js.ExportSymbol
  def start() {

    val m = HashMap("one"->"baz", "two"->"bar", "three"->"foo")

    m filter { _._2.startsWith("b") } foreach { x => println(x._2) }
  }
}
