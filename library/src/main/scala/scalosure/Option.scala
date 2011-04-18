package scalosure

abstract class Option {
  def isEmpty():Boolean
  def get():Any
  def map(fn: Any => Any):Option = if(isEmpty) new None else new Some(fn(this.get))
  def getOrElse(value: => Any):Any = if(isEmpty) value else this.get
}

class None extends Option {
  def isEmpty() = true
  def get() = null
}

case class Some(x:Any) extends Option {
  def isEmpty() = false
  def get() = x
}
