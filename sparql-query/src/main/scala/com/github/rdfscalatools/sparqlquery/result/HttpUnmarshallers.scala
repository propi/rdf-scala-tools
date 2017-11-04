package com.github.rdfscalatools.sparqlquery.result

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshaller
import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery.FromResponseWithAcceptUnmarshaller

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object HttpUnmarshallers {

  implicit val fromResponseCodeToBooleanUnmarshaller: FromResponseWithAcceptUnmarshaller[Boolean] = None -> Unmarshaller.strict[HttpResponse, Boolean](_.status.isSuccess())

}