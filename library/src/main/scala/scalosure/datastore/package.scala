package scalosure

package object datastore {

  import s2js._
  import scalosure.collection.mutable.HashMap

  val collections = new HashMap[Collection[Entity]](JsObject.empty[Collection[Entity]])

  def get[A <: Entity](name:String):Collection[A] = {
    collections.get(name).asInstanceOf[Collection[A]]
  }

  def register[A <: Entity](name:String, url:String):Unit = {

    val xio = new scalosure.net.XhrIo("http://localhost:8080/sample.json", "GET")

    xio.onComplete = { e => 
      collections.push(name, (new Collection[A](e.responseAs[JsObject[A]])).asInstanceOf[Collection[Entity]])
    }
  }

  def update(fn: () => Unit) {
    
  }

}
