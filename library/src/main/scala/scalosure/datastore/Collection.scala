package scalosure.datastore

import s2js._

class Collection[A <: Entity](underlying:JsObject[A]) {

  def foreach(fn:((String,A))=>Unit):Unit = {
    underlying.foreach(fn)
  }
}

object Collection {
  def apply[A <: Entity](xs:(String,A)*):Collection[A] = new Collection[A](JsObject(xs:_*))
}
