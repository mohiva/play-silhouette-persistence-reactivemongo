import com.typesafe.sbt.SbtScalariform._
import xerial.sbt.Sonatype._

import scalariform.formatter.preferences._

//*******************************
// Play settings
//*******************************

name := "play-silhouette-persistence-reactivemongo"

version := "5.0.0"

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.12.3", "2.11.11")

resolvers += Resolver.jcenterRepo

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.5-play26",
  "net.codingwell" %% "scala-guice" % "4.1.0" % "test",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % "test",
  specs2 % Test
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).disablePlugins(PlayLayoutPlugin)

//*******************************
// Compiler settings
//*******************************

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

//*******************************
// Test settings
//*******************************

parallelExecution in Test := false

fork in Test := true

// Needed to avoid https://github.com/travis-ci/travis-ci/issues/3775 in forked tests
// in Travis with `sudo: false`.
// See https://github.com/sbt/sbt/issues/653
// and https://github.com/travis-ci/travis-ci/issues/3775
javaOptions += "-Xmx1G"

//*******************************
// Maven settings
//*******************************

sonatypeSettings

organization := "com.mohiva"

description := "ReactiveMongo persistence module for Silhouette"

homepage := Some(url("http://silhouette.mohiva.com/"))

licenses := Seq("Apache License" -> url("https://github.com/mohiva/play-silhouette-persistence-reactivemongo/blob/master/LICENSE"))

val pom = <scm>
    <url>git@github.com:mohiva/play-silhouette-persistence-reactivemongo.git</url>
    <connection>scm:git:git@github.com:mohiva/play-silhouette-persistence-reactivemongo.git</connection>
  </scm>
    <developers>
      <developer>
        <id>akkie</id>
        <name>Christian Kaps</name>
        <url>http://mohiva.com</url>
      </developer>
    </developers>;

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

sources in (Compile,doc) := Seq.empty

pomExtra := pom

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)
