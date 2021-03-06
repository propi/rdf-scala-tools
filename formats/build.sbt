name := "formats"

organization := "com.github.propi.rdf-scala-tools"

version := "1.2.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val common = "com.github.propi.rdf-scala-tools" %% "common" % "1.2.0"
val jena = "org.apache.jena" % "jena-arq" % "3.6.0"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.6.9"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.2.1"
val akkaJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.1"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

libraryDependencies ++= Seq(common, jena, akkaStream, akkaHttp, akkaJson, scalaLogging)

enablePlugins(SbtTwirl)