package scalosure.datastore

import scalosure._
import scalosure.net._
import scalosure.collection.mutable.List

class Entity(val key:String)

case class Config(name:String, url:String)

object manager {

  val cols = JsObject.empty[Collection]
  val configs = JsObject.empty[Any]

  var async = true

  def get(name:String):Collection = cols(name)

  def download(c:Config, cb:XhrIoCallback) = {

    val xhr = new XhrIo(c.url, "GET", async)

    xhr.onComplete = (e:XhrIoEvent) => {
      cols(c.name) = new Collection(e.responseAs[JsObject[Entity]], c)
      cb.requestComplete()
    }
  
    xhr.send("")
  }

  def register(configs:JsArray[Config], cb: () => Unit) {

    val xhrCallback = new XhrIoCallback(configs.length, cb)

    var i = 0

    while(i < configs.length) {
      val c = configs(i)
      download(c, xhrCallback)
      i = i + 1
    }
  }

  def execute(query:Query, callback:(List) => Unit) {

    val xhr = new XhrIo(query.encoded())

    xhr.onComplete = (e:XhrIoEvent) => {
      callback(new List(e.responseAs[JsArray[Any]]))
    }
  
    xhr.send("")
  }
}

