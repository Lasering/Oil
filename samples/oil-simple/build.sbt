name := """oil-simple"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "oil" %% "oil" % "1.0.0-SNAPSHOT"
)
