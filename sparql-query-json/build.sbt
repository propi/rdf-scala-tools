name := "sparql-query-json"

organization := "com.github.rdf-scala-tools"

version := "1.0.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

val sparqlQuery = "com.github.rdf-scala-tools" %% "sparql-query" % "1.0.0"
val akkaJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9"
val jena = "org.apache.jena" % "jena-arq" % "3.4.0"

libraryDependencies ++= Seq(sparqlQuery, akkaJson, jena)