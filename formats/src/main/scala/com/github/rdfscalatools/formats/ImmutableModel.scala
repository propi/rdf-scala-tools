package com.github.rdfscalatools.formats

import java.io.ByteArrayInputStream

import akka.http.scaladsl.model.{ContentType, MediaType}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}

import scala.util.Try

/**
  * Created by Vaclav Zeman on 18. 11. 2017.
  */
case class ImmutableModel(contentType: ContentType, data: Array[Byte]) {

  def model(implicit mediaTypeToJenaLang: MediaType => Lang = RdfMediaTypes.mediaTypeToJenaLang): Option[Model] = {
    val model = ModelFactory.createDefaultModel()
    Try {
      RDFDataMgr.read(model, new ByteArrayInputStream(data), contentType.mediaType)
      model
    }.toOption
  }

  def cached(implicit mediaTypeToJenaLang: MediaType => Lang = RdfMediaTypes.mediaTypeToJenaLang): Option[ImmutableModel.Cached] = model.map(ImmutableModel.Cached(contentType, data, _))

}

object ImmutableModel {

  case class Cached(contentType: ContentType, data: Array[Byte], model: Model) {
    def toImmutableModel: ImmutableModel = ImmutableModel(contentType, data)
  }

  object Model {
    def unapply(arg: ImmutableModel): Option[Model] = arg.model
  }

}
