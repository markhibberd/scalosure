import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

  override def repositories = Set(ScalaToolsSnapshots,
    "routely" at "http://efleming969.github.com/repo")

  override def libraryDependencies = super.libraryDependencies ++ Set(
    "org.scalosure" % "scalosure-builder" % "0.2-SNAPSHOT")
}  
