name := "sparql-query-rdf4j"

organization := "com.github.propi.rdf-scala-tools"

version := "1.4.1"

scalaVersion := "2.13.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val sparqlQuery = "com.github.propi.rdf-scala-tools" %% "sparql-query" % "1.4.1"

libraryDependencies ++= Seq(sparqlQuery)