package scalosure.collection.mutable

class HashMap(xs:s2js.JsArray) {
  def foo() {
    println("foo") 
  }
}

object HashMap {
  def apply(elems:(String,Any)*) = new HashMap(null)
}
