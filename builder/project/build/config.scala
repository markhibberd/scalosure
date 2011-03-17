import sbt._

class ProjectConfig(info: ProjectInfo) extends PluginProject(info) {

  override def repositories = Set(ScalaToolsSnapshots,
    "routely" at "http://efleming969.github.com/repo")

  override def libraryDependencies = Set(
    "com.google.javascript" % "closure-compiler" % "20110119",
    "org.scalatest"         % "scalatest"        % "1.1" % "test")
}
