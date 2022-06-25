ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

ThisBuild / name := "ohc4s"

ThisBuild / organization := "com.thebinarysoul"

val scalatestVersion = "3.2.12"
val ohcVersion = "0.7.4"

libraryDependencies ++= Seq(
  "org.caffinitas.ohc" % "ohc-core" % ohcVersion exclude("org.apache.logging.log4j", "log4j-core"),
  "org.scalatest" %% "scalatest" % scalatestVersion % Test
)

