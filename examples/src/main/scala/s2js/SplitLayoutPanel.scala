package demo

import browser._
import s2js._
import goog.dom._
import goog.ui.Component

class SplitLayoutPanel(var first:Component, var second:Component) extends Component {

    addChild(first)
    addChild(second)

    override def createDom() {

        val firstEl = first.getElement
        val secondEl = second.getElement

        goog.dom.classes.add(firstEl, "split-left")
        goog.dom.classes.add(secondEl, "split-right")

        setElementInternal(Html(
            <div class="split">{firstEl}{secondEl}</div>))
    }
}

// vim: set ts=4 sw=4 et:
