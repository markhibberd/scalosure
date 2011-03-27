package experimental

import s2js._

class AnyMap(underlying:JsObject[Any]=JsObject.empty[Any]) {

  def +=(kv:(String,Any)):Unit = push(kv._1, kv._2)

  def push(key:String, value:Any):Unit = {
    underlying(key) = value
  }

  def foreach(fn:((String,Any))=>Unit):Unit = {
    underlying.foreach { x => fn(x) }
  }
  
  def map(fn:((String,Any))=>Any):AnyMap = {
    val newmap = new AnyMap(JsObject.empty[Any])
    underlying foreach { x => newmap.push(x._1, x._2) }
    newmap
  }

  def filter(fn:((String,Any))=>Boolean):AnyMap = {
    val newmap = new AnyMap(JsObject.empty[Any])
    underlying foreach { x => if(fn(x)) newmap.push(x._1, x._2) }
    newmap
  }

  def get(key:String):Any = {
    underlying(key).asInstanceOf[Any]
  }
}

object AnyMap {
  def apply(es:(String,Any)*) = new AnyMap(JsObject(es:_*))
}
