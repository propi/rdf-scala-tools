name := "formats"

organization := "com.github.propi.rdf-scala-tools"

version := "1.4.0"

scalaVersion := "2.13.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val common = "com.github.propi.rdf-scala-tools" %% "common" % "1.3.0"
val jena = "org.apache.jena" % "jena-arq" % "4.4.0"
val akkaStream = "com.typesafe.akka" %% "akka-stream-typed" % "2.6.14"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.2.4"
val akkaJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3"

libraryDependencies ++= Seq(common, jena, akkaStream, akkaHttp, akkaJson, scalaLogging)

enablePlugins(SbtTwirl)