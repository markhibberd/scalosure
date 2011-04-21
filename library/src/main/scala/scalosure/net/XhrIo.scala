package scalosure.net

import browser._

class XhrIoCallback(num:Long, cb: () => Unit) {

  var completed = 0

  def requestComplete() {
    completed = completed + 1
    if(completed == num) cb()
  }
}

class XhrIoEvent(val xhr:XMLHttpRequest) {
  def responseAs[A]():A = JSON.parse(xhr.responseText).asInstanceOf[A]
}

class XhrIo(url:String, method:String = "GET", async:Boolean = true) {

  val xhr = new XMLHttpRequest

  xhr.onreadystatechange = () => {
    if(xhr.readyState == 4) {
      onComplete(new XhrIoEvent(xhr))
    } else {
      onError(new XhrIoEvent(xhr))
    }
  }

  var onError:Function1[XhrIoEvent, Unit] = (event) => {}
  var onComplete:Function1[XhrIoEvent, Unit] = (event) => {}

  def send(data:String) {
    xhr.open(method, url, async)
    xhr.send(data)
  }
}

