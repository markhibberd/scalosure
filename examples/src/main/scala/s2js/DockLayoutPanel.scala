package demo

import s2js._
import goog.dom._
import goog.ui.Component

class DockLayoutPanel extends goog.ui.Component {

    var west:Component = null
    var center:Component = null

    override def createDom() {

        super.createDom()

		val el = getElement

        el.innerHTML = Html(<div>Border Layout</div>).innerHTML
    }
}

// vim: set ts=4 sw=4 et:
