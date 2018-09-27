package com.github.rdfscalatools.formats

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import akka.http.scaladsl.model.{ContentType, MediaType}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr, RDFFormat}

import scala.util.Try

/**
  * Created by Vaclav Zeman on 18. 11. 2017.
  */
case class ImmutableModel private(lang: String, data: Array[Byte]) {

  @transient lazy val contentType: ContentType = {
    val jsonld = Lang.JSONLD.getName
    val ntriples = Lang.NTRIPLES.getName
    lang match {
      case `jsonld` => RdfMediaTypes.`application/ld+json`
      case `ntriples` => RdfMediaTypes.`application/n-triples`
      case _ => RdfMediaTypes.`text/turtle`
    }
  }

  def withContentType(contentType: ContentType)(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): ImmutableModel = if (contentType.mediaType == this.contentType.mediaType) {
    this
  } else {
    this.model.map { model =>
      val bos = new ByteArrayOutputStream()
      RDFDataMgr.write(bos, model, contentType.mediaType)
      ImmutableModel(contentType, bos.toByteArray)
    }.getOrElse(this)
  }

  def model(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): Option[Model] = {
    val model = ModelFactory.createDefaultModel()
    Try {
      RDFDataMgr.read(model, new ByteArrayInputStream(data), contentType.mediaType.getLang)
      model
    }.toOption
  }

  def cached(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): Option[ImmutableModel.Cached] = model.map(ImmutableModel.Cached(contentType, data, _))

}

object ImmutableModel {

  def apply(contentType: ContentType, data: Array[Byte])(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): ImmutableModel = new ImmutableModel(contentType.mediaType.getLang.getName, data)

  case class Cached(contentType: ContentType, data: Array[Byte], model: Model) {
    def toImmutableModel: ImmutableModel = ImmutableModel(contentType, data)
  }

  object Model {
    def unapply(arg: ImmutableModel): Option[Model] = arg.model
  }

}
