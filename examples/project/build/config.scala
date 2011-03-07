import sbt._

class ProjectConfig(info: ProjectInfo) extends DefaultWebProject(info) with AutoCompilerPlugins {

	override def repositories = Set(ScalaToolsSnapshots)

    val sxr = compilerPlugin("org.scalosure" %% "scalosure-compiler" % "0.2-SNAPSHOT")

    override def compileOptions = CompileOption("-P:scalosure:input:demo") ::
        CompileOption("-P:scalosure:output:" + outputPath / "scalosure") :: super.compileOptions.toList

    override def extraWebappFiles = (outputPath ##) / "scalosure" ** "*.js"

    override def libraryDependencies = super.libraryDependencies ++ Set(

        /* scala dependencies */
        "org.scalosure" %% "scalosure-tools"    % "0.2-SNAPSHOT",
        "org.scalosure" %% "scalosure-adapters" % "0.2-SNAPSHOT",

        /* java dependencies */
        "org.eclipse.jetty" % "jetty-webapp"     % "7.0.2.RC0" % "test",
        "javax.servlet"     % "servlet-api"      % "2.5"       % "provided")
}

// vim: set ts=4 sw=4 et:
