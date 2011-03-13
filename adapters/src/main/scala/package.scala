package s2js

case class Html(elem:xml.Elem) extends browser.Element

object JsObject {
  def apply(elems:(String,Any)*) = null
}

object JsArray {
  def apply(elems:Any*) = null
}
