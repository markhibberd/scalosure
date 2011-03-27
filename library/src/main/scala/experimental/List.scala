package experimental

import s2js._

class List[A](underlying:JsArray=JsArray()) {

  def +=(value:A):Unit = push(value)

  def push(value:A):Unit = {
    underlying.push(value)
  }

  def foreach(fn:(A)=>Unit):Unit = {
    underlying.forEach { (x,y,z) => fn(x.asInstanceOf[A]) }
  }
  
  def map(fn:(A)=>A):List[A] = {
    val newlist = new List[A](JsArray())
    underlying forEach { (x,y,z) => newlist.push(x.asInstanceOf[A]) }
    newlist
  }

  def filter(fn:(A)=>Boolean):List[A] = {
    val newlist = new List[A](JsArray())
    underlying forEach { (x,y,z) => if(fn(x.asInstanceOf[A])) newlist.push(x.asInstanceOf[A]) }
    newlist
  }
}

object List {
  def apply[A](elems: A*) = new List[A](JsArray(elems:_*))
}

