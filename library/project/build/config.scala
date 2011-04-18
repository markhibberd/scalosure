import sbt._

import org.scalosure.ScalosureProject

class ProjectConfig(info: ProjectInfo) extends DefaultWebProject(info) with ScalosureProject {

  override def repositories = Set(ScalaToolsSnapshots)

  val scalosurePackages = List("scalosure")

  val scalaDependencies = Set(
    "org.scalosure" %% "scalosure-tools"    % "0.2-SNAPSHOT",
    "org.scalosure" %% "scalosure-adapters" % "0.2-SNAPSHOT",
    "org.scalatest" %  "scalatest"          % "1.3" % "test")

  val javaDependencies = Set(
    "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.RC0" % "test",
    "javax.servlet"     % "servlet-api"  % "2.5"       % "provided")

  override def libraryDependencies = super.libraryDependencies ++ scalaDependencies ++ javaDependencies
}
