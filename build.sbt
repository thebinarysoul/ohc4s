ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.3"
ThisBuild / name := "ohc4s"
ThisBuild / organization := "com.thebinarysoul"

val scalatestVersion = "3.2.12"
val ohcVersion = "0.7.4"

lazy val commonSettings = Seq(
  homepage := Some(url("https://github.com/thebinarysoul/ohc4s")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  Test / parallelExecution := false,
  Global / parallelExecution := false,
  scalafmtOnCompile := true
)

lazy val core = Project("ohc4s-core", file("modules/core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.caffinitas.ohc" % "ohc-core" % ohcVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    )
  )

publish / skip := true
