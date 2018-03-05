name := "sparql-query"

organization := "com.github.propi.rdf-scala-tools"

version := "1.1.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val formats = "com.github.propi.rdf-scala-tools" %% "formats" % "1.1.0"

libraryDependencies ++= Seq(formats)

enablePlugins(SbtTwirl)