package com.github.rdfscalatools.sparqlqueryjson

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling._
import com.github.rdfscalatools.common.CommonExceptions
import com.github.rdfscalatools.common.extractors.{AnyToBoolean, AnyToDouble, AnyToInt}
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes
import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.sparqlquery.result.SparqlResult
import spray.json._
import spray.json.DefaultJsonProtocol._

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object SparqlResultJsonUnmarshallers {

  implicit def fromResponseWithAcceptUnmarshallerToJsValue: FromResponseWithAcceptUnmarshaller[JsValue] = {
    val mediaType = RdfMediaTypes.`application/sparql-results+json`
    implicit val un: FromEntityUnmarshaller[JsValue] = Unmarshaller.byteStringUnmarshaller.forContentTypes(mediaType).andThen(SprayJsonSupport.sprayJsValueByteStringUnmarshaller)
    Some(mediaType) -> implicitly[FromResponseUnmarshaller[JsValue]]
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
      .map(_.asJsObject.fields.mapValues(jsValueToResultValue))
      .toIndexedSeq

  }

}
