package com.github.rdfscalatools.sparqlqueryjson

import java.io.ByteArrayInputStream

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes
import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery.FromResponseWithAcceptUnmarshaller
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import spray.json.JsValue

/**
  * Created by Vaclav Zeman on 31. 8. 2017.
  */
object JsonLdUnmarshallers {

  private val mediaType = RdfMediaTypes.`application/ld+json`

  implicit def fromResponseWithAcceptUnmarshallerToJenaModel: FromResponseWithAcceptUnmarshaller[Model] = {
    implicit val un: FromEntityUnmarshaller[Model] = Unmarshaller.byteArrayUnmarshaller.forContentTypes(mediaType).map { byteArray =>
      val model = ModelFactory.createDefaultModel()
      RDFDataMgr.read(model, new ByteArrayInputStream(byteArray), Lang.JSONLD)
      model
    }
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[Model]]
  }

  implicit def fromResponseWithAcceptUnmarshallerToJson: FromResponseWithAcceptUnmarshaller[JsValue] = {
    implicit val un: FromEntityUnmarshaller[JsValue] = Unmarshaller.byteStringUnmarshaller.forContentTypes(mediaType).andThen(SprayJsonSupport.sprayJsValueByteStringUnmarshaller)
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[JsValue]]
  }

}
