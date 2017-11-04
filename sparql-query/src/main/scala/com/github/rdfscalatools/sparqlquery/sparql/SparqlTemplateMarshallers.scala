package com.github.rdfscalatools.sparqlquery.sparql

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{HttpCharset, HttpEntity, MessageEntity}
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object SparqlTemplateMarshallers {

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