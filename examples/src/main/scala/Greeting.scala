package demo

import goog.dom._

import goog.ui.Component

class Greeting extends goog.ui.Component {

    def sayHello(name:String) {
        getElement.innerHTML = "changed"
    }

    override def createDom() {
        super.createDom()

		val el = getElement

        el.innerHTML = <h1>Hello, World</h1>.toString
    }
}

        //val aButton = new Button("Say Hello")

        //goog.events.listen(aButton, Component.EventType.ACTION, (e:Event) => {
            //g.sayHello("superman")
        //})

        //g.render(root)
        //aButton.render(root)
// vim: set ts=4 sw=4 et:
