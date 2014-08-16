// This import is for the keys in PlayScala
import PlayKeys._

name := "plyrhub-ranking"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.livestream" %% "scredis" % "1.1.2",
  "com.netflix.archaius" % "archaius-core" % "0.6.1",
  "com.netflix.archaius" % "archaius-scala" % "0.6.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"
)

addCommandAlias("my-idea", ";update-classifiers;update-sbt-classifiers;idea with-sources=yes")

addCommandAlias("cc", ";clean;compile")