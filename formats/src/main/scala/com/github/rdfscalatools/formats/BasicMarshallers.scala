package com.github.rdfscalatools.formats

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{HttpCharset, HttpEntity, MediaType, MessageEntity}
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import spray.json.{CompactPrinter, JsValue, JsonPrinter}

/**
  * Created by Vaclav Zeman on 14. 11. 2017.
  */
object BasicMarshallers {

  implicit def fromJenaModelToEntityMarshaller(implicit mediaTypeToJenaLang: MediaType => Lang, additionalMediaTypes: List[MediaType.WithFixedCharset] = Nil): ToEntityMarshaller[Model] = {
    Marshaller.oneOf(RdfMediaTypes.defaultFormats ::: additionalMediaTypes: _*) { mediaType =>
      Marshaller.byteArrayMarshaller(mediaType).compose[Model] { model =>
        val bos = new ByteArrayOutputStream()
        RDFDataMgr.write(bos, model, mediaType)
        bos.toByteArray
      }
    }
  }

  implicit def fromJsonToEntityMarshaller(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[JsValue] = {
    SprayJsonSupport.sprayJsValueMarshaller.wrap(RdfMediaTypes.`application/ld+json`)(x => x)
  }

  /**
    * Default sparql template marshaller.
    * It can be mapped to sparql update by `map` function: sparqlMarshaller map toSparqlUpdate
    */
  implicit val sparqlMarshaller: ToEntityMarshaller[SparqlTemplate.Sparql] = Marshaller.opaque[SparqlTemplate.Sparql, MessageEntity] { sparql =>
    HttpEntity(RdfMediaTypes.`application/sparql-query`, sparql.toString())
  }

  def toSparqlUpdate(entity: MessageEntity): MessageEntity = entity.withContentType(RdfMediaTypes.`application/sparql-update`)

  def toSparqlQueryWithCharset(httpCharset: HttpCharset)(entity: MessageEntity): MessageEntity = entity.withContentType(RdfMediaTypes.`application/sparql-query; charset=`.toContentType(httpCharset))

  def toSparqlUpdateWithCharset(httpCharset: HttpCharset)(entity: MessageEntity): MessageEntity = entity.withContentType(RdfMediaTypes.`application/sparql-update; charset=`.toContentType(httpCharset))

}
