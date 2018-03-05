name := "sparql-query-tdb"

organization := "com.github.propi.rdf-scala-tools"

version := "1.1.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "jitpack" at "https://jitpack.io"

val sparqlQuery = "com.github.propi.rdf-scala-tools" %% "sparql-query" % "1.1.0"
val jenaTdb2 = "org.apache.jena" % "jena-tdb2" % "3.6.0"

libraryDependencies ++= Seq(sparqlQuery, jenaTdb2)
        