import java.net.URL

name         := "ij-plugins_imageio"
organization := "net.sf.ij-plugins"
version := "2.0.0-SNAPSHOT" // + svnRevision.value.revision

homepage := Some(new URL("https://ij-plugins.sf.net"))
startYear := Some(2002)
licenses := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))
description := "<html>" +
    "IJ Plugins Toolkit is a set of ImageJ plugins grouped into:" +
    "<ul>" +
    "<li>3D IO - import and export of data in 3D formats.</li>" +
    "<li>3D Toolkit - operations on stacks interpreted as 3D images, including morphological operations.</li>" +
    "<li>Color - color space conversion, color edge detection (color and multi-band images).</li>" +
    "<li>Filters - fast median filters and various anisotropic diffusion filters.</li>" +
  "<li>Graphics - Texture Synthesis - A plugins to perform texture synthesis using the image quilting algorithm of Efros and Freeman.</li>" +
    "<li>Segmentation - image segmentation through clustering, thresholding, and region growing.</li>" +
    "</ul>" +
    "</html>"

libraryDependencies ++= Seq(
  "net.imagej"        % "ij"                % "1.49v",
  //  "com.github.jai-imageio" % "jai-imageio-core" % "1.3.0",
  // Test
  "junit"             % "junit"             % "4.12" % "test",
  // JUnit runner SBT plugins
  "com.novocode"      % "junit-interface"   % "0.11" % "test->default"
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
javacOptions ++= Seq("-Xlint")

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> "}

//
// Setup sbt-imagej plugins
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir := "sandbox"
ijPluginsSubDir := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles += ijPluginsDir.value

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
pomExtra :=
    <scm>
      <url>https://sourceforge.net/p/ij-plugins/code/</url>
      <connection>scm:svn://svn.code.sf.net/p/ij-plugins/code/trunk/ij-plugins</connection>
    </scm>
    <developers>
      <developer>
        <id>jsacha</id>
        <name>Jarek Sacha</name>
      </developer>
    </developers>
