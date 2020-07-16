package com.github.rdfscalatools.formats

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import akka.http.scaladsl.model.{ContentType, MediaType}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.system.StreamRDFLib
import org.apache.jena.riot.{Lang, RDFDataMgr, RDFFormat, RDFParser}

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

  def withContentType(contentType: ContentType)(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat, mode: ImmutableModel.Mode = ImmutableModel.Mode.Default): ImmutableModel = if (contentType.mediaType == this.contentType.mediaType) {
    this
  } else {
    this.model.map { model =>
      val bos = new ByteArrayOutputStream()
      RDFDataMgr.write(bos, model, contentType.mediaType)
      ImmutableModel(contentType, bos.toByteArray)
    }.getOrElse(this)
  }

  def model(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat, mode: ImmutableModel.Mode = ImmutableModel.Mode.Default): Option[org.apache.jena.rdf.model.Model] = {
    val model = ModelFactory.createDefaultModel()
    Try {
      val stream = StreamRDFLib.graph(model.getGraph)
      val checking = mode == ImmutableModel.Mode.Default
      RDFParser.create()
        .source(new ByteArrayInputStream(data))
        .base(null)
        .lang(contentType.mediaType.getLang)
        .context(null)
        .checking(checking)
        .parse(stream)
      model
    }.toOption
  }

  def cached(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat, mode: ImmutableModel.Mode = ImmutableModel.Mode.Default): Option[ImmutableModel.Cached] = model.map(ImmutableModel.Cached(contentType, data, _))

}

object ImmutableModel {

  sealed trait Mode

  object Mode {

    case object Default extends Mode

    case object NoChecking extends Mode

  }

  def apply(contentType: ContentType, data: Array[Byte])(implicit mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): ImmutableModel = new ImmutableModel(contentType.mediaType.getLang.getName, data)

  case class Cached(contentType: ContentType, data: Array[Byte], model: org.apache.jena.rdf.model.Model) {
    def toImmutableModel: ImmutableModel = ImmutableModel(contentType, data)
  }

  object Model {
    def unapply(arg: ImmutableModel): Option[org.apache.jena.rdf.model.Model] = arg.model
  }

  object NoCheckingModel {
    def unapply(arg: ImmutableModel): Option[org.apache.jena.rdf.model.Model] = {
      implicit val noChecking: Mode = Mode.NoChecking
      arg.model
    }
  }

}