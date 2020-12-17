package com.github.rdfscalatools.sparqlqueryrdf4j

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.{QueryBuilder, QueryOperation, Transaction}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object RepositoryQueryBuilder {

  type QB[T] = QueryBuilder[SparqlTemplate.Sparql, T, Transaction.Empty.type]

  implicit def anyQueryBuilder[T](implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: Materializer, repository: Repository, unmarshaller: FromResponseWithAcceptUnmarshaller[T], requester: HttpRequest => Future[HttpResponse]): QB[T] = (_: Transaction.Empty.type) => new RepositoryOneQuery[T]

  implicit def resultTableQueryBuilder[T](implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: Materializer, repository: Repository, resultTableUnmarshaller: FromResponseWithAcceptUnmarshaller[ResultTable], tableToAny: ResultTable => T, requester: HttpRequest => Future[HttpResponse]): QB[T] = (transaction: Transaction.Empty.type) => {
    val oq = anyQueryBuilder[ResultTable].apply(transaction)
    (operation: QueryOperation, input: SparqlTemplate.Sparql) => oq.execute(operation, input).map(tableToAny)
  }

}