package s2js.tools

import scala.io._
import scala.util.matching._

import scala.collection.{
  JavaConversions => JC, mutable => mu
}

import java.io._

trait DepsFile {

  def it2json(xs:Iterator[String]) = xs.mkString("['", "','","']")

  def buildList(file:File, re:Regex) = re.findAllIn(
    io.Source.fromFile(file).getLines.mkString).matchData map { m => m.group(1) }

  def jsDepedencies(path:String, base:String):String = {

    val paths = tree(new File(path)).filter(_.getName.endsWith(".js"))

    val sb = new StringBuilder

    paths foreach { path =>

      val prvs = buildList(path, """goog\.provide\(\s*['\"]([^'\"]+)['\"]\s*\);""".r)

      val reqs = buildList(path, """goog\.require\(\s*['\"]([^'\"]+)['\"]\s*\);""".r )

      val template = "goog.addDependency('../../..%s', %s, %s);" + System.getProperty("line.separator") 

      sb.append(template.format(path.getAbsolutePath.stripPrefix(base).replace("\\", "/"), it2json(prvs), it2json(reqs)))
    }

    return sb.toString
  }

  def tree(file:java.io.File):List[File] = if(file.isDirectory) {
    file.listFiles.toList.flatMap(tree) 
  } else List(file)

}

