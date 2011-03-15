package demo

import goog.dom._
import goog.history._
import goog.events

object app {

  val history = new goog.History

  @s2js.ExportSymbol
  def start() {

    val shell = new MainShell(history)

    shell.render(getElement("mainShell"))

    events.listen(history, EventType.NAVIGATE, (e:Event) => { println(e.token) })

    history.setEnabled(true)
  }
}
