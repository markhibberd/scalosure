package scalosure.net

import browser._

import goog.json

class XhrIoEvent(val xhr:XMLHttpRequest) {
  def responseAs[A]():A = goog.json.parse(xhr.responseText).asInstanceOf[A]
}

class XhrIo(
  url:String, 
  method:String = "POST", 
  data:String = "", 
  async:Boolean = true) {

  val xhr = new XMLHttpRequest

  xhr.onreadystatechange = () => { 
    xhr.readyState match {
      case 4 => onComplete(new XhrIoEvent(xhr))
      case _ =>
    }
  }

  var onComplete:Function1[XhrIoEvent, Unit] = (event) => {}

  def send(data:String) {
    xhr.open(method, url, async)
    xhr.send(data)
  }

}

