package demo

import s2js._
import goog.dom._
import goog.events
import goog.ui._
import goog.math._
import goog.pubsub._
import goog.history._

class MainShell(history:goog.History) extends Component {

    override def createDom() {
        super.createDom()
    }

    override def enterDocument() {
        super.enterDocument

        events.listen(history, EventType.NAVIGATE, (e:Event) => {
            println("inside main shell")
        })

    }
}

// vim: set ts=4 sw=4 et:
