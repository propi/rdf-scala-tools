package com.github.rdfscalatools.sparqlquery.query

import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future

/**
  * Created by Vaclav Zeman on 18. 11. 2017.
  */
object SparqlTemplateOps {

  private val logger = Logger[SparqlTemplateOps.type]

  private def execute[T](operation: QueryOperation, query: SparqlTemplate.Sparql, oneQuery: OneQuery[SparqlTemplate.Sparql, T]): Future[T] = {
    if (logger.underlying.isTraceEnabled) {
      logger.trace(operation.toString)
      logger.trace(query.toString())
    }
    oneQuery.execute(operation, query)
  }

  implicit class PimpedSparqlTemplate(query: SparqlTemplate.Sparql) {
    def select[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transaction: T): Future[O] = execute(QueryOperation.Select, query, queryBuilder(transaction))

    def construct[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transaction: T): Future[O] = execute(QueryOperation.Construct, query, queryBuilder(transaction))

    def describe[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transaction: T): Future[O] = execute(QueryOperation.Describe, query, queryBuilder(transaction))

    def ask[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, T], transaction: T): Future[Boolean] = execute(QueryOperation.Ask, query, queryBuilder(transaction))

    def update[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Unit, T], transaction: T): Future[Unit] = execute(QueryOperation.Update, query, queryBuilder(transaction))
  }

}