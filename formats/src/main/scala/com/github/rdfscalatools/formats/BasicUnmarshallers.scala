package com.github.rdfscalatools.formats

import java.io.ByteArrayInputStream

import akka.http.scaladsl.model.{ContentTypeRange, HttpResponse, MediaType}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import com.github.rdfscalatools.formats.ImmutableModel.Cached
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}

import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 14. 11. 2017.
  */
object BasicUnmarshallers {

  type FromResponseWithAcceptUnmarshaller[T] = (Option[MediaType], FromResponseUnmarshaller[T])

  implicit def fromResponseWithAcceptUnmarshallerToResponseUnmarshaller[T](un: FromResponseWithAcceptUnmarshaller[T]): FromResponseUnmarshaller[T] = un._2

  implicit def fromEntityToCached(implicit mediaTypeToJenaLang: MediaType => Lang, additionalMediaTypes: List[ContentTypeRange] = Nil): FromEntityUnmarshaller[Cached] = {
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(RdfMediaTypes.defaultFormats.map(x => x: ContentTypeRange) ::: additionalMediaTypes: _*).mapWithInput { (entity, byteArray) =>
      val model = ModelFactory.createDefaultModel()
      RDFDataMgr.read(model, new ByteArrayInputStream(byteArray), entity.contentType.mediaType)
      Cached(entity.contentType, byteArray, model)
    }
  }

  implicit def fromEntityToImmutableModelUnmarshaller(implicit un: FromEntityUnmarshaller[Cached]): FromEntityUnmarshaller[ImmutableModel] = {
    un.map(_.toImmutableModel)
  }

  implicit def fromEntityToJenaModelUnmarshaller(implicit un: FromEntityUnmarshaller[Cached]): FromEntityUnmarshaller[Model] = {
    un.map(_.model)
  }

  implicit def fromResponseWithAcceptToAnyUnmarshaller[T](implicit mediaType: MediaType.WithFixedCharset, un: FromEntityUnmarshaller[T]): FromResponseWithAcceptUnmarshaller[T] = {
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[T]]
  }

  implicit val fromResponseCodeToBooleanUnmarshaller: FromResponseWithAcceptUnmarshaller[Boolean] = None -> Unmarshaller.strict[HttpResponse, Boolean](_.status.isSuccess())

}
