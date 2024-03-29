name := "sparql-query"

organization := "com.github.propi.rdf-scala-tools"

version := "1.4.1"

scalaVersion := "2.13.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val formats = "com.github.propi.rdf-scala-tools" %% "formats" % "1.4.1"

libraryDependencies ++= Seq(formats)

enablePlugins(SbtTwirl)