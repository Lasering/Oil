//organization := "io.github.lasering"

name := "Oil"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.adrianhurt" %% "play-bootstrap3" % "0.4",
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "jquery-validation" % "1.13.1",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)

licenses += ("Apache v2", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

//pipelineStages := Seq(digest, gzip)

