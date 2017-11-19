name := "rdf-scala-tools"

val basicSettings = Seq(
  organization := "com.github.propi",
  version := "1.0.0",
  scalaVersion := "2.12.2",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")
)

lazy val root = project
  .in(file("."))
  .settings(basicSettings: _*)
  .aggregate(common, formats, sparqlQuery, sparqlQueryRdf4j)

lazy val common = project
  .in(file("common"))

lazy val formats = project
  .in(file("formats"))
  .dependsOn(common)

lazy val sparqlQuery = project
  .in(file("sparql-query"))
  .dependsOn(formats)

lazy val sparqlQueryRdf4j = project
  .in(file("sparql-query-rdf4j"))
  .dependsOn(sparqlQuery)
