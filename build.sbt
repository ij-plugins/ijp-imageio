// @formatter:off
import java.net.URL

name         := "ijp_imageio"
organization := "net.sf.ij-plugins"
version      := "2.1.0"

homepage     := Some(new URL("https://github.com/ij-plugins/ijp-imageio"))
organizationHomepage := Some(url("http://ij-plugins.sf.net"))
startYear    := Some(2002)
licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))
description  := "ijp-ImageIO enable reading and writing images using Java ImageIO codecs. " +
  "The core ImageIO formats: JPEG, PNG, BMP, WBMP, and GIF. IJP-ImageIO is also using JAI codes adding support for " +
  "TIFF, JPEG200, PNM, and PCX. TIFF supports reading and writing using various compression schemes: LZW, JPEG, ZIP, " +
  "and Deflate. For more detailed information see IJP-ImageIO home page: http://ij-plugins.sf.net/plugins/imageio."


libraryDependencies ++= Seq(
  "net.imagej"             % "ij"                   % "1.52t",
  "junit"                  % "junit"                % "4.13"  % "test",
  "com.novocode"           % "junit-interface"      % "0.11"  % "test->default"
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
javacOptions in(Compile, compile) ++= Seq("-Xlint")
javacOptions in(Compile, doc    ) ++= Seq(
  "-windowtitle", "IJP-ImageIO API v." + version.value,
  "-header",      "IJP-ImageIO API v." + version.value,
  "-sourcepath",  (baseDirectory.value / "src/main/java").getAbsolutePath,
  "-subpackages", "net.sf.ij_plugins.imageio",
  "-exclude",     "net.sf.ij_plugins.imageio.impl:net.sf.ij_plugins.imageio.plugins",
  "-verbose"
)

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> " }

//
// Setup sbt-imagej plugins
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles              += ijPluginsDir.value

baseDirectory in run := baseDirectory.value / "sandbox"

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true
// This is a Java project, disable using the Scala version in output paths and artifacts
crossPaths        := false
// This forbids including Scala related libraries into the dependency
autoScalaLibrary  := false

publishArtifact in(Test, packageBin) := false
publishArtifact in(Test, packageDoc) := false
publishArtifact in(Test, packageSrc) := false

publishTo := version {
  version: String =>
    if (version.contains("-SNAPSHOT"))
      Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else
      Some("Sonatype Nexus Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}.value

// @formatter:on

pomExtra :=
  <scm>
    <url>https://github.com/ij-plugins/ijp-imageio</url>
    <connection>https://github.com/ij-plugins/ijp-imageio.git</connection>
  </scm>
    <developers>
      <developer>
        <id>jpsacha</id>
        <name>Jarek Sacha</name>
        <url>https://github.com/jpsacha</url>
      </developer>
    </developers>
