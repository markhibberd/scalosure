import sbt._

class ProjectConfig(info:ProjectInfo) extends DefaultProject(info) {

    override def repositories = Set(ScalaToolsSnapshots)

    override def libraryDependencies = super.libraryDependencies ++ Set(

        /* scala dependencies */
        "org.scalatest"  % "scalatest"   % "1.3" % "test",
        /* java dependencies */
        "javax.servlet"  % "servlet-api" % "2.5" % "provided")
}

// vim: set ts=4 sw=4 et:
