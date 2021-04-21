package com.github.rdfscalatools.formats

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import com.github.rdfscalatools.common.CommonExceptions
import com.github.rdfscalatools.common.extractors.{AnyToBoolean, AnyToDouble, AnyToInt}
import com.github.rdfscalatools.formats.BasicUnmarshallers.{FromResponseWithAcceptUnmarshaller, fromResponseWithAcceptToAnyUnmarshaller}
import com.github.rdfscalatools.formats.result.SparqlResult
import spray.json.{JsArray, JsString, JsValue, RootJsonReader, jsonReader}
import spray.json.DefaultJsonProtocol._

/**
  * Created by Vaclav Zeman on 21. 11. 2017.
  */
object JsonUnmarshallers {

  implicit def fromEntityToJsonUnmarshaller: FromEntityUnmarshaller[JsValue] = Unmarshaller.byteStringUnmarshaller.forContentTypes(RdfMediaTypes.`application/ld+json`, RdfMediaTypes.`application/sparql-results+json`).andThen(SprayJsonSupport.sprayJsValueByteStringUnmarshaller)

  implicit def fromResponseWithAcceptToJsonAnyUnmarshaller[T](implicit reader: RootJsonReader[T], jsonUnmarshaller: FromResponseWithAcceptUnmarshaller[JsValue]): FromResponseWithAcceptUnmarshaller[T] = {
    jsonUnmarshaller._1 -> jsonUnmarshaller._2.map(jsonReader[T].read)
  }

  implicit def fromResponseWithAcceptToSparqlResultTableUnmarshaller: FromResponseWithAcceptUnmarshaller[SparqlResult.ResultTable] = {
    implicit val mediaType: MediaType.WithFixedCharset = RdfMediaTypes.`application/sparql-results+json`
    fromResponseWithAcceptToJsonAnyUnmarshaller(SparqlResultJsonToResultTableReader, implicitly[FromResponseWithAcceptUnmarshaller[JsValue]])
  }

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
      .map(_.asJsObject.fields.view.mapValues(jsValueToResultValue).toMap)
      .toIndexedSeq

  }

}