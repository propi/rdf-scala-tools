package com.github.rdfscalatools.sparqlquerytriples

import java.io.ByteArrayInputStream

import akka.http.scaladsl.model.{ContentTypeRange, MediaType}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes
import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery.FromResponseWithAcceptUnmarshaller
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}

/**
  * Created by Vaclav Zeman on 14. 11. 2017.
  */
object TriplesUnmarshallers {

  implicit def fromEntityToJenaModelUnmarshaller(implicit mediaTypeToJenaLang: MediaType => Lang, additionalMediaTypes: List[ContentTypeRange] = Nil): FromEntityUnmarshaller[Model] = {
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(List[ContentTypeRange](RdfMediaTypes.`application/ld+json`, RdfMediaTypes.`text/turtle`) ::: additionalMediaTypes: _*).mapWithInput { (entity, byteArray) =>
      val model = ModelFactory.createDefaultModel()
      RDFDataMgr.read(model, new ByteArrayInputStream(byteArray), entity.contentType.mediaType)
      model
    }
  }

  implicit def fromResponseWithAcceptToJenaModelUnmarshaller(implicit mediaType: MediaType.WithFixedCharset, mediaTypeToJenaLang: MediaType => Lang, additionalMediaTypes: List[ContentTypeRange] = Nil): FromResponseWithAcceptUnmarshaller[Model] = {
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[Model]]
  }

}
