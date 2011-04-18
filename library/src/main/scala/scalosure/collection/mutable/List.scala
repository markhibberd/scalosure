package scalosure.collection.mutable

import scalosure._

class List(xs:JsArray[Any]) {

  def foreach(fn:(Any) => Unit):Unit = {
    var i = 0
    while(i < xs.length) {
      fn(xs(i))
      i = i + 1
    }
  }

  def map(fn:(Any) => Any):List = {
      var rst = JsArray.empty[Any]
      foreach { x => rst.push(fn(x)) }
      new List(rst)
  }

  def filter(fn:(Any) => Boolean):List = {
      var rst = JsArray.empty[Any]
      foreach { x => if(fn(x)) rst.push(x) }
      new List(rst)
  }

  def exists(fn:(Any) => Boolean):Boolean = {
      var rst = false
      foreach { x => if(fn(x)) { rst = true } }
      rst
  }

  def append(x:Any):Unit = {
    xs.push(x)
  }

  def toArray():JsArray[Any] = xs
}

