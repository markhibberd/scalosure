import sbt._

class ProjectConfig(info:ProjectInfo) extends DefaultProject(info) {

    val scalosureClasspath = managedClasspath(config("test")) +++ buildLibraryJar
    
    override def repositories = Set(ScalaToolsSnapshots)

    override def libraryDependencies = super.libraryDependencies ++ Set(
        "org.scalosure"  %% "scalosure-adapters" % "0.2-SNAPSHOT" % "test",
        "org.scalatest"   % "scalatest"          % "1.3"          % "test")

    override def testOptions = super.testOptions ++ Seq(
        TestArgument(TestFrameworks.ScalaTest, "-Dcp="+scalosureClasspath.getFiles.mkString(":")),
        TestArgument(TestFrameworks.ScalaTest, "-Doutput="+(outputPath / "scalosure")))
}

// vim: set ts=4 sw=4 et:

