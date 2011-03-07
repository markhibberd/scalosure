package demo

import s2js._
import goog.dom._
import goog.events
import goog.ui._
import goog.math._
import goog.pubsub._
import goog.history._

class MainShell(history:goog.History) extends Component {

    val viewport = new ViewportSizeMonitor
    val navigator = new Navigator
    val content = new Content
    var split:SplitPane = null 

    override def createDom() {
        super.createDom()

        split = new SplitPane(navigator, content, SplitPane.Orientation.HORIZONTAL)

        addChild(split, true)

        split.setHandleSize(2)
        split.setFirstComponentSize(200)
        split.setSize(viewport.getSize)
    }

    override def enterDocument() {
        super.enterDocument

        events.listen(viewport, events.EventType.RESIZE, (e:Event) => {
            split.setSize(viewport.getSize)
        }, false, this)

        events.listen(history, EventType.NAVIGATE, (e:Event) => {
            println("inside main shell")
        })

    }
}

// vim: set ts=4 sw=4 et:
