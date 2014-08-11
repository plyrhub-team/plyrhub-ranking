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
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"
)

