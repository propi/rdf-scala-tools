package com.github.rdfscalatools.sparqlquery.query.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshal, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.sparqlquery.query.{OneQuery, QueryOperation}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 14. 8. 2017.
  */
abstract class HttpOneQuery[I, O](implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: Materializer, marshaller: ToEntityMarshaller[I], unmarshaller: FromResponseWithAcceptUnmarshaller[O], requester: HttpRequest => Future[HttpResponse]) extends OneQuery[I, O] {

  implicit private val un: FromResponseUnmarshaller[O] = unmarshaller._2

  private val outputMediaType = unmarshaller._1

  protected def operationToHttpMethod(operation: QueryOperation): HttpMethod

  protected def operationToUri(operation: QueryOperation): Uri

  protected def beforeRequest(operation: QueryOperation, request: HttpRequest): HttpRequest = request

  protected def afterResponse(operation: QueryOperation, response: HttpResponse): HttpResponse = response

  final def execute(operation: QueryOperation, input: I): Future[O] = Marshal(input).to[MessageEntity].flatMap { entity =>
    val defaultRequest = HttpRequest(
      operationToHttpMethod(operation),
      operationToUri(operation),
      List(
        outputMediaType.map(x => headers.Accept(x))
      ).flatten,
      entity
    )
    requester(beforeRequest(operation, defaultRequest)).flatMap(response => Unmarshal(afterResponse(operation, response)).to[O])
  }

}