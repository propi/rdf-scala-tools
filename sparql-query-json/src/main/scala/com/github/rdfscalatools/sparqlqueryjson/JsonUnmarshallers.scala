package com.github.rdfscalatools.sparqlqueryjson

import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery.FromResponseWithAcceptUnmarshaller
import spray.json._

/**
  * Created by Vaclav Zeman on 31. 8. 2017.
  */
object JsonUnmarshallers {

  implicit def fromResponseWithAcceptUnmarshallerToJsonToAny[T](implicit reader: RootJsonReader[T], jsonUnmarshaller: FromResponseWithAcceptUnmarshaller[JsValue]): FromResponseWithAcceptUnmarshaller[T] = {
    jsonUnmarshaller._1 -> jsonUnmarshaller._2.map(jsonReader[T].read)
  }

}
