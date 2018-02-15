package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query._
import org.apache.jena.rdf.model.Model

import scala.concurrent.{ExecutionContext, Future}

object RepositoryOneQuery {

  class BooleanOneQuery private[sparqlquerytdb](session: RepositorySession) extends OneQuery[SparqlTemplate.Sparql, Boolean] {
    def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[Boolean] = operation match {
      case QueryOperation.Ask => session.execAsk(input.toString())
      case _ => Future.failed(new IllegalArgumentException)
    }
  }

  class ModelOneQuery private[sparqlquerytdb](session: RepositorySession) extends OneQuery[SparqlTemplate.Sparql, Model] {
    def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[Model] = operation match {
      case QueryOperation.Describe => session.execDescribe(input.toString())
      case QueryOperation.Construct => session.execConstruct(input.toString())
      case _ => Future.failed(new IllegalArgumentException)
    }
  }

  class UnitOneQuery private[sparqlquerytdb](session: RepositorySession) extends OneQuery[SparqlTemplate.Sparql, Unit] {
    def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[Unit] = operation match {
      case QueryOperation.Update => session.execUpdate(input.toString())
      case _ => Future.failed(new IllegalArgumentException)
    }
  }

  class ResultTableOneQuery private[sparqlquerytdb](session: RepositorySession) extends OneQuery[SparqlTemplate.Sparql, ResultTable] {
    def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[ResultTable] = operation match {
      case QueryOperation.Select => session.execSelect(input.toString())
      case _ => Future.failed(new IllegalArgumentException)
    }
  }

  class AutoCommitOneQuery[T] private[sparqlquerytdb](oneQuery: RepositoryTransaction => OneQuery[SparqlTemplate.Sparql, T])(implicit ec: ExecutionContext, tb: TransactionBuilder[RepositoryTransaction]) extends OneQuery[SparqlTemplate.Sparql, T] {
    def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[T] = {
      Transaction { implicit tx: RepositoryTransaction =>
        oneQuery(tx).execute(operation, input)
      }
    }
  }

}