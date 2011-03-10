package browser 

object `package` {
	def alert (s:Any) {}
}

/*
class Object extends scala.collection.Map[String, Any] {
	def - (key:String) = super.-(key)
}
*/

class Window {
	def get(key:String) = ""
	object location {
		var href = ""
	}
	def focus () {}
	
	def setTimeout (fn:()=>Unit, milliseconds:Int) {}

    val sessionStorage:Storage = null
    val localStorage:Storage = null
}

object window extends Window {}

class Node 

class Element extends Node {
	val id :String = ""
	var innerHTML = ""
	var className = ""
		
	// TODO: should we have to cast here?
	// inputs 
	var value = ""
		
	// forms
	def submit () {}
}

class HTMLAnchorElement extends Element
class HTMLDivElement extends Element
class HTMLInputElement extends Element
class HTMLFrameElement extends Element
class HTMLInput extends Element {
	def focus () {}
}
class HTMLButton extends Element {
	var disabled = false
}

class Document {
	val body:Element = null
	def execCommand(command:String, showDefaultUI:Boolean, value:String) {}
}

object document extends Document

class Range {
	def insertNode (n:Node) {}
	def surroundContents (n:Node) {}
}

class Storage {
    val length:Long = 0
    def key(index:Long):String = ""
    def getItem(key:String):String = null
    def setItem(key:String, value:String) {}
    def removeItem(key:String) {}
    def clear() {}
}
