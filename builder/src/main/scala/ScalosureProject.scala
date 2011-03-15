package org.scalosure

import sbt._

trait ScalosureProject extends DefaultWebProject with AutoCompilerPlugins {

  val scalosurePackages:List[String]

  val sxr = compilerPlugin("org.scalosure" %% "scalosure-compiler" % "0.2-SNAPSHOT")

  val jsConfig = config("js") hide

  lazy val jsbuild = task { createCustomFile() }

  override def packageAction = super.packageAction dependsOn(jsbuild)

  override def compileOptions = 
    CompileOption("-P:scalosure:input:"+scalosurePackages.mkString(";")) ::
    CompileOption("-P:scalosure:output:" + outputPath / "scalosure") :: 
    super.compileOptions.toList

  override def extraWebappFiles = (outputPath ##) / "scalosure" ** "*.js"

  def createCustomFile() = {

    val fileName = outputPath / "testing-1.0.js" toString

    new java.io.File(fileName).createNewFile()

    None
  }
}

// vim: set ts=4 sw=4 et:
