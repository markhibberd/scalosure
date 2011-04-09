package scalosure

object script {
  def literal(script:String):Unit = {}
}

class JsObject[A] {
  var x:A = _
  def foreach(fn:((String,A))=>Unit):Unit = {}
  def apply(name:String):A = x
  def update(key:String, value:A):Unit = {}
}

object JsObject {
  def apply[A](elems:(String,A)*):JsObject[A] = new JsObject[A]
  def empty[A]():JsObject[A] = new JsObject[A]
}

class ScalosureObject
