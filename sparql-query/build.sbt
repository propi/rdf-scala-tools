name := "sparql-query"

organization := "com.github.propi.rdf-scala-tools"

version := "1.0.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val common = "com.github.propi.rdf-scala-tools" %% "common" % "1.0.0"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.9"

libraryDependencies ++= Seq(common, akkaHttp)

enablePlugins(SbtTwirl)