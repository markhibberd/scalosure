package scalosure.collection

import s2js._

class HashMap[A](underlying:JsObject[A]=JsObject.empty[A]) {

  def oof():Unit = {}

  def +=(kv:(String,A)):Unit = push(kv._1, kv._2)

  def push(key:String, value:A):Unit = {
    underlying(key) = value
  }

  def foreach(fn:((String,A))=>Unit):Unit = {
    underlying.foreach { x => fn(x) }
  }
  
  def map(fn:((String,A))=>A):HashMap[A] = {
    val newmap = new HashMap[A](JsObject.empty[A])
    underlying foreach { x => newmap.push(x._1, x._2) }
    newmap
  }

  def filter(fn:((String,A))=>Boolean):HashMap[A] = {
    val newmap = new HashMap[A](JsObject.empty[A])
    underlying foreach { x => if(fn(x)) newmap.push(x._1, x._2) }
    newmap
  }
}

object HashMap {
  def apply[A](elems:(String,A)*) = new HashMap[A](JsObject[A](elems:_*))
}
