package com.github.rdfscalatools.formats

import akka.http.scaladsl.model.{HttpCharsets, MediaType}
import org.apache.jena.riot.Lang

import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 21. 8. 2017.
  */
object RdfMediaTypes {

  implicit def mediaTypeToJenaLang(mediaType: MediaType): Lang = {
    mediaType match {
      case RdfMediaTypes.`application/ld+json` => Lang.JSONLD
      case RdfMediaTypes.`text/turtle` => Lang.TTL
      case _ => throw new IllegalArgumentException(s"Media type '${mediaType.value}' is not RDF type.")
    }
  }

  val `application/sparql-query`: MediaType.WithFixedCharset = MediaType.applicationWithFixedCharset("sparql-query", HttpCharsets.`UTF-8`, "rq", "sparql")

  val `application/sparql-update`: MediaType.WithFixedCharset = MediaType.applicationWithFixedCharset("sparql-update", HttpCharsets.`UTF-8`, "rq", "sparql")

  val `application/sparql-query; charset=`: MediaType.WithOpenCharset = MediaType.applicationWithOpenCharset("sparql-query", "rq", "sparql")

  val `application/sparql-update; charset=`: MediaType.WithOpenCharset = MediaType.applicationWithOpenCharset("sparql-update", "rq", "sparql")

  val `application/sparql-results+json`: MediaType.WithFixedCharset = MediaType.applicationWithFixedCharset("sparql-results+json", HttpCharsets.`UTF-8`, "srj")

  val `application/ld+json`: MediaType.WithFixedCharset = MediaType.applicationWithFixedCharset("ld+json", HttpCharsets.`UTF-8`, "jsonld")

  val `text/turtle`: MediaType.WithFixedCharset = MediaType.textWithFixedCharset("turtle", HttpCharsets.`UTF-8`, "ttl")

  val defaultFormats: List[MediaType.WithFixedCharset] = List(RdfMediaTypes.`application/ld+json`, RdfMediaTypes.`text/turtle`)

}
