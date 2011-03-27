package s2js

import java.io._

import org.scalatest.{ Spec, BeforeAndAfterAll }

import scala.tools.nsc.{Settings, CompilerCommand}
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}

abstract class PrinterFixtureSpec extends Spec with BeforeAndAfterAll {

    var testdriving = false

    def cleanit(str:String) = str.replaceAll("""([ ]{2,}|[\n])""", "")

    trait Expector {

        val actual:String
        val wrapper:String

        def toBe(jsCode:String) {
            val expected = wrapper.format(jsCode).stripMargin
            expect(cleanit(actual))(cleanit(expected))
        }
    
        def toDebugClean(jsCode:String) {
            println("actual: "+cleanit(actual))
            println()
            println("expect: "+cleanit(jsCode))
        }

        def toDebug(jsCode:String) {
            println("actual: "+actual)
            println()
            println("expect: "+jsCode)
            if(testdriving) {
                val pw = new PrintWriter(new BufferedWriter(new FileWriter("testdriven.html")));
                pw.print(<html>
                    <head>
                        <script type="text/javascript" src="scalosure.js"></script>
                        <script type="text/javascript" src="/home/erick/projects/google-closure-library/closure/goog/base.js"></script>
                    </head>
                    <body>
                        <script type="text/javascript">
                            scalosureInit();
                            {actual}
                            o.start();
                        </script>
                    </body>
                </html>.toString)
                pw.close
            }
        }
    }

    class S2JSParser(settings:Settings, reporter:Reporter) extends Global(settings, reporter) with S2JSPrinter {

        def this(settings:Settings) = this(settings, new ConsoleReporter(settings))

        val global = this
        
        def parse(sCode:String):String = {
            val cr = new TyperRun()
            val sf = new BatchSourceFile("<init>", sCode)
            val bd = typedTree(sf, true).asInstanceOf[global.Tree]
            tree2string(bd)
        }

        def expect(sCode:String):Expector = new Expector {
            val actual = parse(sCode)
            val wrapper = "%s"
        }

        // wraps a snippet in an object
        def expectSnippet(scalaCode:String):Expector = new Expector {
            val actual = parse("package $pkg { object $obj { %s }}".format(scalaCode))
            val wrapper = "goog.provide('$pkg.$obj');$pkg.$obj.%s"
        }
    }

    var parser:S2JSParser = _

    override def beforeAll(configMap: Map[String, Any]) {
        val dir = new java.io.File(configMap("output").toString)
        dir.mkdir

        val settings = new Settings

        settings.classpath.tryToSet(configMap("cp").toString :: Nil)

        parser = new S2JSParser(settings)
    }

    override def afterAll {
        parser.askShutdown
    }
}

// vim: set ts=4 sw=4 et:
