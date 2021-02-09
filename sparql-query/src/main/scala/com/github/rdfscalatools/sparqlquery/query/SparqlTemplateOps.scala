package com.github.rdfscalatools.sparqlquery.query

import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by Vaclav Zeman on 18. 11. 2017.
  */
object SparqlTemplateOps {

  private val logger = Logger[SparqlTemplateOps.type]

  private def execute[T](operation: QueryOperation, query: SparqlTemplate.Sparql, oneQuery: OneQuery[SparqlTemplate.Sparql, T])(implicit ec: ExecutionContext): Future[T] = {
    val start = System.nanoTime()
    val f = oneQuery.execute(operation, query)
    if (logger.underlying.isTraceEnabled || logger.underlying.isDebugEnabled) {
      f.onComplete { x =>
        val duration = Duration.fromNanos(System.nanoTime() - start)
        val msg = x match {
          case Success(_) => s"has been successful within ${duration.toMillis} ms."
          case Failure(th) => s"has been failed within ${duration.toMillis} ms (${th.getClass.getSimpleName}: ${th.getMessage})."
        }
        if (logger.underlying.isTraceEnabled) {
          logger.trace(s"Query operation '$operation' $msg\n$query")
        } else if (duration.toMillis >= 500) {
          logger.debug(s"Query operation '$operation' $msg\n$query")
        }
      }
    }
    f
  }

  implicit class PimpedSparqlTemplate(query: SparqlTemplate.Sparql) {
    def select[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transaction: T, ec: ExecutionContext): Future[O] = execute(QueryOperation.Select, query, queryBuilder(transaction))

    def construct[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transaction: T, ec: ExecutionContext): Future[O] = execute(QueryOperation.Construct, query, queryBuilder(transaction))

    def describe[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transaction: T, ec: ExecutionContext): Future[O] = execute(QueryOperation.Describe, query, queryBuilder(transaction))

    def ask[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, T], transaction: T, ec: ExecutionContext): Future[Boolean] = execute(QueryOperation.Ask, query, queryBuilder(transaction))

    def update[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Unit, T], transaction: T, ec: ExecutionContext): Future[Unit] = execute(QueryOperation.Update, query, queryBuilder(transaction))
  }

}