package com.github.rdfscalatools.sparqlquerytriples

import akka.http.scaladsl.model.MediaType
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes
import org.apache.jena.riot.Lang

import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 14. 11. 2017.
  */
object MediaTypeConversions {

  implicit def mediaTypeToJenaLang(mediaType: MediaType): Lang = {
    mediaType match {
      case RdfMediaTypes.`application/ld+json` => Lang.JSONLD
      case RdfMediaTypes.`text/turtle` => Lang.TTL
      case _ => throw new IllegalArgumentException(s"Media type '${mediaType.value}' is not RDF type.")
    }
  }

}
