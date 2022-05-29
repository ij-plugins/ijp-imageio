// @formatter:off
import java.net.URL

name         := "ijp_imageio"
organization := "net.sf.ij-plugins"
version      := "2.3.0.1-SNAPSHOT"

homepage     := Some(new URL("https://github.com/ij-plugins/ijp-imageio"))
organizationHomepage := Some(url("https://github.com/ij-plugins"))
startYear    := Some(2002)
licenses     := Seq(("LGPL-2.1", new URL("https://opensource.org/licenses/LGPL-2.1")))
description  := "ijp-ImageIO enable reading and writing images using Java ImageIO codecs. " +
  "The core ImageIO formats: JPEG, PNG, BMP, WBMP, and GIF. IJP-ImageIO is also using JAI codes adding support for " +
  "TIFF, JPEG200, PNM, and PCX. TIFF supports reading and writing using various compression schemes: LZW, JPEG, ZIP, " +
  "and Deflate. For more detailed information see IJP-ImageIO home page: https://github.com/ij-plugins/ijp-imageio/wiki."

libraryDependencies ++= Seq(
  "net.imagej"             % "ij"                   % "1.53s",
  "junit"                  % "junit"                % "4.13.2"  % "test",
  "com.novocode"           % "junit-interface"      % "0.11"    % "test->default"
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
Compile / compile / javacOptions ++= Seq(
  "-Xlint",
  "--release", "11"
)
Compile / doc / javacOptions ++= Seq(
  "-windowtitle", "IJP-ImageIO API v." + version.value,
  "-header",      "IJP-ImageIO API v." + version.value,
  "-sourcepath",  (baseDirectory.value / "src/main/java").getAbsolutePath,
  "-subpackages", "ij_plugins.imageio",
  "-exclude",     "ij_plugins.imageio.impl:ij_plugins.imageio.plugins",
  "-verbose"
)

//
// Setup sbt-imagej plugins
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles              += ijPluginsDir.value

run / baseDirectory := baseDirectory.value / "sandbox"

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true
// This is a Java project, disable using the Scala version in output paths and artifacts
crossPaths        := false
// This forbids including Scala related libraries into the dependency
autoScalaLibrary  := false
publishTo         := sonatypePublishToBundle.value
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-imageio", "jpsacha@gmail.com"))
developers := List(
  Developer(id="jpsacha", name="Jarek Sacha", email="jpsacha@gmail.com", url=url("https://github.com/jpsacha"))
)

Test / packageBin / publishArtifact := false
Test / packageDoc / publishArtifact := false
Test / packageSrc / publishArtifact := false
