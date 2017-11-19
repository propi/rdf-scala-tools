package com.github.rdfscalatools.formats

import java.io.ByteArrayInputStream

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypeRange, HttpResponse, MediaType}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import com.github.rdfscalatools.common.CommonExceptions
import com.github.rdfscalatools.common.extractors.{AnyToBoolean, AnyToDouble, AnyToInt}
import com.github.rdfscalatools.formats.ImmutableModel.Cached
import com.github.rdfscalatools.formats.result.SparqlResult
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import spray.json.{JsArray, JsString, JsValue, RootJsonReader, jsonReader}
import spray.json.DefaultJsonProtocol._

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

  implicit def fromEntityToJsonUnmarshaller: FromEntityUnmarshaller[JsValue] = Unmarshaller.byteStringUnmarshaller.forContentTypes(RdfMediaTypes.`application/ld+json`, RdfMediaTypes.`application/sparql-results+json`).andThen(SprayJsonSupport.sprayJsValueByteStringUnmarshaller)

  implicit def fromResponseWithAcceptToAnyUnmarshaller[T](implicit mediaType: MediaType.WithFixedCharset, un: FromEntityUnmarshaller[T]): FromResponseWithAcceptUnmarshaller[T] = {
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[T]]
  }

  implicit def fromResponseWithAcceptToJsonAnyUnmarshaller[T](implicit reader: RootJsonReader[T], jsonUnmarshaller: FromResponseWithAcceptUnmarshaller[JsValue]): FromResponseWithAcceptUnmarshaller[T] = {
    jsonUnmarshaller._1 -> jsonUnmarshaller._2.map(jsonReader[T].read)
  }

  implicit val fromResponseCodeToBooleanUnmarshaller: FromResponseWithAcceptUnmarshaller[Boolean] = None -> Unmarshaller.strict[HttpResponse, Boolean](_.status.isSuccess())

  implicit object SparqlResultJsonToResultTableReader extends RootJsonReader[SparqlResult.ResultTable] {

    private def jsValueToResultValue(jsValue: JsValue): SparqlResult = {
      val integerSuffix = List("int", "integer", "short", "byte", "positiveInteger", "negativeInteger", "nonNegativeInteger", "nonPositiveInteger", "unsignedByte", "unsignedInt", "unsignedShort")
      val doubleSuffix = List("decimal", "double", "float", "long", "unsignedLong")
      val fields = jsValue.asJsObject.fields
      val value = fields.get("value").collect {
        case JsString(x) => x
      }
      val typedValue: Option[SparqlResult] = fields.get("type").collect {
        case JsString("uri") => value.map(SparqlResult.Uri)
        case JsString("literal") => fields.get("datatype") match {
          case Some(JsString(t)) =>
            if (integerSuffix.exists(t.endsWith)) {
              value.collect { case AnyToInt(number) => SparqlResult.IntLiteral(number) }
            } else if (doubleSuffix.exists(t.endsWith)) {
              value.collect { case AnyToDouble(number) => SparqlResult.DoubleLiteral(number) }
            } else if (t.endsWith("boolean")) {
              value.map { case AnyToBoolean(bool) => SparqlResult.BooleanLiteral(bool) }
            } else {
              value.map(SparqlResult.StringLiteral)
            }
          case _ => value.map(SparqlResult.StringLiteral)
        }
      }.flatten
      typedValue.getOrElse(throw CommonExceptions.DeserializationException(s"Json value '$jsValue' can not be parsed."))
    }

    def read(json: JsValue): SparqlResult.ResultTable = json.asJsObject.fields.get("results")
      .flatMap(_.asJsObject.fields.get("bindings"))
      .iterator
      .flatMap(_.convertTo[JsArray].elements)
      .map(_.asJsObject.fields.mapValues(jsValueToResultValue))
      .toIndexedSeq

  }

}
