package demo

import goog.dom._
import goog.ui.Component

class Content extends goog.ui.Component {

    override def createDom() {

        super.createDom()

		val el = getElement

        goog.dom.classes.add(el, "content")

        el.innerHTML = s2js.Html(<div>Main Content</div>).innerHTML
    }
}

// vim: set ts=4 sw=4 et:


