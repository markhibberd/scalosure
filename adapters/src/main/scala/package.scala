package s2js

case class Html(elem:xml.Elem) extends browser.Element

object JsObject {
  def apply(elems:(String,Any)*) = null
}

class JsArray {
  //def indexOf()
  //def lastIndexOf()
  //def every()
  def forEach(fn:(Any, Long, JsArray)=>Unit) {}
  def map(fn:(Any, Long, JsArray)=>Any) = new JsArray
  def filter(fn:(Any, Long, JsArray)=>Boolean) = new JsArray
  //def some()
}

object JsArray {
  def apply(elems:Any*) = new JsArray
}
