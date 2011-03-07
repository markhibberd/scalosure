package s2js.tools

import org.scalatest._

class DepsFileSpec extends Spec with DepsFile {

    describe("DepsFile") {

        it("should build a dependency script from a directory of files") {
          
            val base = new java.io.File("src/test/resources").getAbsolutePath

            val deps = jsDepedencies(base+"/scripts", base)
            
            println(deps)
        }
    }
}

// vim: set ts=4 sw=4 et:

