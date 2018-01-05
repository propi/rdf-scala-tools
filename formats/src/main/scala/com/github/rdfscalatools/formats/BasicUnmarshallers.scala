package com.github.rdfscalatools.formats

import akka.http.scaladsl.model.{ContentTypeRange, HttpResponse, MediaType}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import com.github.rdfscalatools.formats.ImmutableModel.Cached
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.RDFFormat

import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 14. 11. 2017.
  */
object BasicUnmarshallers {

  type FromResponseWithAcceptUnmarshaller[T] = (Option[MediaType], FromResponseUnmarshaller[T])

  implicit def fromResponseWithAcceptUnmarshallerToResponseUnmarshaller[T](un: FromResponseWithAcceptUnmarshaller[T]): FromResponseUnmarshaller[T] = un._2

  implicit def fromEntityToImmutableModelUnmarshaller(implicit additionalMediaTypes: List[ContentTypeRange] = Nil): FromEntityUnmarshaller[ImmutableModel] = {
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(RdfMediaTypes.defaultFormats.map(x => x: ContentTypeRange) ::: additionalMediaTypes: _*).mapWithInput { (entity, byteArray) =>
      ImmutableModel(entity.contentType, byteArray)
    }
  }

  implicit def fromEntityToCachedUnmarshaller(implicit un: FromEntityUnmarshaller[ImmutableModel], mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): FromEntityUnmarshaller[Cached] = {
    un.map(_.cached.getOrElse(throw Unmarshaller.NoContentException))
  }

  implicit def fromEntityToJenaModelUnmarshaller(implicit un: FromEntityUnmarshaller[ImmutableModel], mediaTypeToJenaFormat: MediaType => RDFFormat = RdfMediaTypes.mediaTypeToJenaFormat): FromEntityUnmarshaller[Model] = {
    un.map(_.model.getOrElse(throw Unmarshaller.NoContentException))
  }

  implicit def fromResponseWithAcceptToAnyUnmarshaller[T](implicit mediaType: MediaType.WithFixedCharset, un: FromEntityUnmarshaller[T]): FromResponseWithAcceptUnmarshaller[T] = {
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[T]]
  }

  implicit val fromResponseCodeToBooleanUnmarshaller: FromResponseWithAcceptUnmarshaller[Boolean] = None -> Unmarshaller.strict[HttpResponse, Boolean](_.status.isSuccess())

}
