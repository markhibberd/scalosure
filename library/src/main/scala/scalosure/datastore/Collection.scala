package scalosure.datastore

import browser._

import scalosure._
import scalosure.net._

import scalosure.collection.mutable.List

class Collection(data:JsObject[Entity], config:Config) {

  def get(key:String):Entity = data(key)

  def put(entity:Entity, callback: (Entity) => Unit) = {

    val xhr = if(entity.key == null) {
      new XhrIo(config.url, "POST")
    } else {
      new XhrIo(config.url+"/"+entity.key, "POST")
    }

    xhr.onComplete = (e:XhrIoEvent) => {

      val ent = e.responseAs[Entity]

      data(ent.key) = ent

      callback(ent)
    }
  
    xhr.send(JSON.stringify(entity))
  }

  def foreach(fn:(Entity) => Unit):Unit = {
    data.foreach(x => fn(x._2))
  }

  def map(fn:(Entity) => Entity):List = {

    val tmp = JsArray.empty[Any]

    data.foreach {
      x => tmp.push(fn(x._2))
    }

    new List(tmp)
  }

  def filter(fn:(Entity) => Boolean):List = {

    val tmp = JsArray.empty[Any]

    data.foreach { x => 
      if(fn(x._2)) tmp.push(x._2)
    } 

    new List(tmp)
  }
  
  def toArray():JsArray[Entity] = {
    val tmp = JsArray.empty[Entity]
    data.foreach { x => tmp.push(x._2) }
    tmp
  }

}

