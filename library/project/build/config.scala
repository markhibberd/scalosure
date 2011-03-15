import sbt._

class ProjectConfig(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {

  override def repositories = Set(ScalaToolsSnapshots)

  val sxr = compilerPlugin("org.scalosure" %% "scalosure-compiler" % "0.2-SNAPSHOT")

  override def compileOptions = CompileOption("-P:scalosure:input:scala") ::
    CompileOption("-P:scalosure:output:" + outputPath / "scalosure") :: 
    super.compileOptions.toList

  override def libraryDependencies = super.libraryDependencies ++ Set(

    /* scala dependencies */
    "org.scalosure" %% "scalosure-tools"    % "0.2-SNAPSHOT",
    "org.scalosure" %% "scalosure-adapters" % "0.2-SNAPSHOT")
}
