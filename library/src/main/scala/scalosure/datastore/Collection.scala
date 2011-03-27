package scalosure.datastore

import s2js._

class Collection[A <: Entity](obj:JsObject[A]) extends scalosure.collection.mutable.HashMap[A](obj) {

}

