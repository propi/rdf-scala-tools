name := "sparql-query-rdf4j"

organization := "com.github.rdf-scala-tools"

version := "1.0.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

val sparqlQuery = "com.github.rdf-scala-tools" %% "sparql-query" % "1.0.0"

libraryDependencies ++= Seq(sparqlQuery)
        