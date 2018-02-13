package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.{OneQuery, QueryBuilder, QueryOperation}

import scala.concurrent.ExecutionContext

/**
  * Created by Vaclav Zeman on 12. 2. 2018.
  */
object RepositoryQueryBuilder {

  implicit def anyQueryBuilder[T](implicit session: RepositorySession, datasetOps: DatasetOps[T], ec: ExecutionContext): QueryBuilder[SparqlTemplate.Sparql, T, RepositoryTransaction] = new QueryBuilder[SparqlTemplate.Sparql, T, RepositoryTransaction] {
    def apply(): OneQuery[SparqlTemplate.Sparql, T] = new RepositoryOneQuery[T](session)

    def apply(transaction: RepositoryTransaction): OneQuery[SparqlTemplate.Sparql, T] = new RepositoryOneQuery[T](transaction.session)
  }

  implicit def resultTableQueryBuilder[T](implicit session: RepositorySession, datasetOps: DatasetOps[ResultTable], ec: ExecutionContext, tableToAny: ResultTable => T): QueryBuilder[SparqlTemplate.Sparql, T, RepositoryTransaction] = new QueryBuilder[SparqlTemplate.Sparql, T, RepositoryTransaction] {
    def apply(): OneQuery[SparqlTemplate.Sparql, T] = {
      val query = anyQueryBuilder[ResultTable].apply()
      (operation: QueryOperation, input: SparqlTemplate.Sparql) => query.execute(operation, input).map(tableToAny)
    }

    def apply(transaction: RepositoryTransaction): OneQuery[SparqlTemplate.Sparql, T] = {
      val query = anyQueryBuilder[ResultTable].apply(transaction)
      (operation: QueryOperation, input: SparqlTemplate.Sparql) => query.execute(operation, input).map(tableToAny)
    }
  }

}