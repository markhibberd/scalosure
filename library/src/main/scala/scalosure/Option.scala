package scalosure

abstract class Option {
  def isEmpty():Boolean
  def get():Any
  def map(fn: Any => Any):Option = if(isEmpty) None else Some(fn(this.get))
  def getOrElse(value: => Any):Any = if(isEmpty) value else this.get
}

object None extends Option {
  def isEmpty() = true
  def get() = null
}

case class Some(x:Any) extends Option {
  def isEmpty() = false
  def get() = x
}
