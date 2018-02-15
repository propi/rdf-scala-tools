package com.github.rdfscalatools.sparqlqueryrdf4j

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.{QueryBuilder, QueryOperation, Transaction}

import scala.concurrent.ExecutionContext

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object RepositoryQueryBuilder {

  type QB[T] = QueryBuilder[SparqlTemplate.Sparql, T, Transaction.Empty.type]

  implicit def anyQueryBuilder[T](implicit actorSystem: ActorSystem, materializer: Materializer, repository: Repository, unmarshaller: FromResponseWithAcceptUnmarshaller[T]): QB[T] = (_: Transaction.Empty.type) => new RepositoryOneQuery[T]

  implicit def resultTableQueryBuilder[T](implicit actorSystem: ActorSystem, materializer: Materializer, repository: Repository, resultTableUnmarshaller: FromResponseWithAcceptUnmarshaller[ResultTable], tableToAny: ResultTable => T): QB[T] = (transaction: Transaction.Empty.type) => {
    val oq = anyQueryBuilder[ResultTable].apply(transaction)
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    (operation: QueryOperation, input: SparqlTemplate.Sparql) => oq.execute(operation, input).map(tableToAny)
  }

}