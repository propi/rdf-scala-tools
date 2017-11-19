package com.github.rdfscalatools.formats

import akka.http.scaladsl.model.{ContentType, HttpEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers._
import com.github.rdfscalatools.formats.RdfMediaTypes._
import org.apache.jena.rdf.model.Model

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Vaclav Zeman on 18. 11. 2017.
  */
case class ImmutableModel(contentType: ContentType, data: Array[Byte]) {

  def model(implicit materializer: Materializer, ec: ExecutionContext): Future[Model] = Unmarshal[HttpEntity](HttpEntity(contentType, data)).to[Model]

  def cached(implicit materializer: Materializer, ec: ExecutionContext): Future[ImmutableModel.Cached] = model.map(ImmutableModel.Cached(contentType, data, _))

}

object ImmutableModel {

  case class Cached(contentType: ContentType, data: Array[Byte], model: Model) {
    def toImmutableModel: ImmutableModel = ImmutableModel(contentType, data)
  }

}
