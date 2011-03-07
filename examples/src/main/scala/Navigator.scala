package demo

import goog.dom._
import goog.ui.Component

class Navigator extends goog.ui.Component {

    override def createDom() {

        super.createDom()

		val el = getElement

        goog.dom.classes.add(el, "navigator")

        goog.dom.appendChild(el, s2js.Html(<ul>
          <li><a href="#home">Home</a></li>
          <li><a href="#components">Components</a></li>
        </ul>))
    }
}

// vim: set ts=4 sw=4 et:

