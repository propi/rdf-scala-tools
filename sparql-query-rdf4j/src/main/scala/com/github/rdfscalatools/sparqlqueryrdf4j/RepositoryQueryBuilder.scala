package com.github.rdfscalatools.sparqlqueryrdf4j

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.{OneQuery, QueryBuilder, QueryOperation, Transaction}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
abstract class RepositoryQueryBuilder[O] extends QueryBuilder[SparqlTemplate.Sparql, O, Transaction.Empty.type] {
  def apply(transaction: Transaction.Empty.type): OneQuery[SparqlTemplate.Sparql, O] = apply()
}

object RepositoryQueryBuilder {

  implicit def anyQueryBuilder[T](implicit actorSystem: ActorSystem, materializer: Materializer, repository: Repository, unmarshaller: FromResponseWithAcceptUnmarshaller[T]): RepositoryQueryBuilder[T] = new RepositoryQueryBuilder[T] {
    def apply(): OneQuery[SparqlTemplate.Sparql, T] = new RepositoryOneQuery[T] {}
  }

  implicit def resultTableQueryBuilder[T](implicit actorSystem: ActorSystem, materializer: Materializer, repository: Repository, resultTableUnmarshaller: FromResponseWithAcceptUnmarshaller[ResultTable], tableToAny: ResultTable => T): RepositoryQueryBuilder[T] = new RepositoryQueryBuilder[T] {
    private val tqb = anyQueryBuilder[ResultTable]
    implicit private val ec: ExecutionContext = actorSystem.dispatcher

    def apply(): OneQuery[SparqlTemplate.Sparql, T] = new OneQuery[SparqlTemplate.Sparql, T] {
      private val oq = tqb()

      def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[T] = oq.execute(operation, input).map(tableToAny)
    }
  }

}