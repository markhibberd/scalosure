package testing

import goog.dom._
import goog.events

import scalosure.collection.mutable._

object app {

  @s2js.ExportSymbol
  def start() {

    val m = HashMap("one"->"foo")

    println(m.foo())
  }
}
