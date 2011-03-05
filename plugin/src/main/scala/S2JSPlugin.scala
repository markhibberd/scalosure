package s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

import java.io.{File, FileWriter, BufferedWriter}

class S2JSPlugin (val global:Global) extends Plugin {

	val name = "s2js"
	val description = "Scala-to-Javascript compiler plugin"
	val runsAfter = List("refchecks")

	val components = List[PluginComponent](Component)

	var output = "."
    var input = ""
	
	override def processOptions(options:List[String], error:String=>Unit) {

        val optionsMap = options.foldLeft(Map.empty[String,String]) {
            (a, b) => a ++ Map(b.split(":").head -> b.split(":").last)
        }

        output = optionsMap.getOrElse("output", "")
        input = optionsMap.getOrElse("input", "")

		// validate
		if (output == "") error("You must provide an [output] option")
		if (input == "") error("You must provide an [input] option")
	}

	private object Component extends PluginComponent with S2JSPrinter {

		val global = S2JSPlugin.this.global
		val phaseName = S2JSPlugin.this.name

        import global._

        val runsAfter = List("typer")

        def newPhase(prev:Phase) = new StdPhase(prev) {
            
            override def name = phaseName

            override def apply(unit:CompilationUnit) = {

                if(unit.body.symbol.fullName.startsWith(input)) {

                    val path = unit.body.symbol.fullName.replace('.', '/')
                    val fullPath = unit.source.file.path

                    val newFilePath = output+"/"+fullPath.slice(fullPath.indexOfSlice(path), fullPath.size).replace(".scala",".js")
                    new File(newFilePath).getParentFile.mkdirs

                    var stream = new FileWriter(newFilePath)
                    var writer = new BufferedWriter(stream)

                    writer write tree2string(unit.body)
                    writer.close()
                }
            }
        }
	}
}

// vim: set ts=4 sw=4 et:
