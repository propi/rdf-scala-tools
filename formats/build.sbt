name := "formats"

organization := "com.github.propi.rdf-scala-tools"

version := "1.0.2"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val common = "com.github.propi.rdf-scala-tools" %% "common" % "1.0.2"
val jena = "org.apache.jena" % "jena-arq" % "3.6.0"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.9"
val akkaJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

libraryDependencies ++= Seq(common, jena, akkaHttp, akkaJson, scalaLogging)

enablePlugins(SbtTwirl)