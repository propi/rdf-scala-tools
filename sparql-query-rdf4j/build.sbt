name := "sparql-query-rdf4j"

organization := "com.github.propi.rdf-scala-tools"

version := "1.0.2"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val sparqlQuery = "com.github.propi.rdf-scala-tools" %% "sparql-query" % "1.0.2"

libraryDependencies ++= Seq(sparqlQuery)
        