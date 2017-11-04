package com.github.rdfscalatools.sparqlqueryjson

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import spray.json.{CompactPrinter, JsValue, JsonPrinter}

/**
  * Created by Vaclav Zeman on 31. 8. 2017.
  */
object JsonLdMarshallers {

  private val mediaType = RdfMediaTypes.`application/ld+json`

  implicit def fromJsonToEntityMarshaller(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[JsValue] = {
    SprayJsonSupport.sprayJsValueMarshaller.wrap(mediaType)(x => x)
  }

  implicit def fromJenaModelToEntityMarshaller: ToEntityMarshaller[Model] = {
    Marshaller.byteArrayMarshaller(mediaType).compose[Model] { model =>
      val bos = new ByteArrayOutputStream()
      RDFDataMgr.write(bos, model, Lang.JSONLD)
      bos.toByteArray
    }
  }

}
