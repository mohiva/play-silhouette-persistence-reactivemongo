import scalariform.formatter.preferences._
import xerial.sbt.Sonatype._

//*******************************
// Play settings
//*******************************

name := "play-silhouette-persistence-reactivemongo"

version := "4.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "4.0.0-SNAPSHOT",
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0-SNAPSHOT",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.0" % "test",
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
// Maven settings
//*******************************

sonatypeSettings

organization := "com.mohiva"

description := ""

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

publishArtifact in (Compile, packageDoc) := false

publishArtifact in packageDoc := false

sources in (Compile,doc) := Seq.empty

pomExtra := pom

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(PreserveDanglingCloseParenthesis, true)
