import sbt._

class ProjectConfig(info:ProjectInfo) extends DefaultProject(info) {

    val s2jsClasspath = managedClasspath(config("test")) +++ buildLibraryJar
    
    override def repositories = Set(ScalaToolsSnapshots)

    override def libraryDependencies = super.libraryDependencies ++ Set(
        "s2js"          %% "s2js-api"  % "0.2-SNAPSHOT" % "test",
        "org.scalatest"  % "scalatest" % "1.3"          % "test")

    override def testOptions = super.testOptions ++ Seq(
        TestArgument(TestFrameworks.ScalaTest, "-Dcp="+s2jsClasspath.getFiles.mkString(":")),
        TestArgument(TestFrameworks.ScalaTest, "-Doutput="+(outputPath / "s2js")))
}

// vim: set ts=4 sw=4 et:

