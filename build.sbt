ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

ThisBuild / name := "ohc4s"

ThisBuild / organization := "com.thebinarysoul"

val scalatestVersion = "3.2.12"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion % Test
)

