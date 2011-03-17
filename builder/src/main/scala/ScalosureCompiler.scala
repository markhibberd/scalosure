package org.scalosure

import scala.collection.jcl.Conversions._
import scala.util.matching._
import scala.io.Source

import java.io.File

import java.{util => jutil}

import com.google.javascript.jscomp._

import CompilationLevel._

object ScalosureCompiler {

  case class JsThing(file:File, reqs:Set[String])

  def deepListOfFiles(file:File):List[File] = if(file.isDirectory) {
      file.listFiles.toList.flatMap(deepListOfFiles) 
  } else List(file)

  def findAllInSource(source:Source, re:Regex) = re.findAllIn(source.getLines.mkString).matchData map { m => m.group(1) }

  def providesFromSource(source:Source) = 
    findAllInSource(source, """goog\.provide\(\s*['\"]([^'\"]+)['\"]\s*\);""".r)

  def requiresFromSource(source:Source):Set[String] = 
    findAllInSource(source, """goog\.require\(\s*['\"]([^'\"]+)['\"]\s*\);""".r).foldLeft(Set.empty[String]) { _ + _ }

  def providesFromFile(file:File) = providesFromSource(Source.fromFile(file)) map {
    provide => (provide -> file)
  }

  def thingFromFile(file:File) = JsThing(file, requiresFromSource(Source.fromFile(file)))

  def requiredThings(jsThings:Map[String, JsThing], rootThingName:String):List[JsThing] = {

      val visited = new collection.mutable.ArrayBuffer[JsThing]
      val sorted = new collection.mutable.ArrayBuffer[JsThing]

      def visit(thing:JsThing) {
        if(!visited.contains(thing)) {
          visited += thing
          thing.reqs foreach { jsThings.get(_) map { visit } }
          sorted += thing
        }
      }

      jsThings.get(rootThingName) map { visit }

      return sorted.toList
  }

  def compileJsFromPath(path:String, rootPackage:String) = {

    val jsFiles = deepListOfFiles(new File(path)) filter { _.getName.endsWith(".js") }

    val provides = jsFiles map { providesFromFile } flatMap { _.toList } 
    
    val thingsThatAreRequired = requiredThings(provides.foldLeft(Map.empty[String,JsThing]) {
        (m, p) => m + (p._1 -> thingFromFile(p._2)) 
    }, rootPackage)

    val options = new CompilerOptions

    ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options)

    options.prettyPrint = true

    val closure = new Compiler

    val jsSources = thingsThatAreRequired map { thing => JSSourceFile.fromFile(thing.file) }

    val jsInputs = List(JSSourceFile.fromFile("lib/google-closure-library/closure/goog/base.js")) ++ jsSources

    val jsExterns = deepListOfFiles(new File("lib/externs")) filter { _.getName.endsWith(".js") } map { f => JSSourceFile.fromFile(f) }

    if(closure.compile(jsExterns.toArray, jsInputs.toArray, options).success)
      println("closure compiled successfully")
    else
      println("closure failed compilation")

    closure.toSource
  }
}

