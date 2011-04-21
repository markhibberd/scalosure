package scalosure.datastore

import scalosure._
import scalosure.collection.mutable.List

class Query(name:String) {
 
  val filters = JsObject.empty[String]
  var _size = 0L
  var _step = 0L

  def filter(name:String, value:String):Query = {
    filters(name) = value   
    this 
  }

  def sliding(size:Long, step:Long):Query = {
    _size = size
    _step = step
    this
  }

  def encoded() = {

    var enFilters = JsArray.empty[String]

    filters.foreach {
      x => enFilters.push(x._1+":"+x._2)
    }

    var encUrl = JsArray.empty[String]

    encUrl.push(enFilters.join("%26"))

    encUrl.push("size=" + _size)
    encUrl.push("step=" + _step)

    "http://localhost:8082/api/" + name + "?q=" + encUrl.join("&")
  }
}
