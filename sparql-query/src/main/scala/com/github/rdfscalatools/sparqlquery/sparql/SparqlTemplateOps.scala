package com.github.rdfscalatools.sparqlquery.sparql

import com.github.rdfscalatools.sparqlquery.query._

import scala.concurrent.Future

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object SparqlTemplateOps {

  def execute[T](operation: QueryOperation, query: SparqlTemplate.Sparql)(implicit oneQuery: OneQuery[SparqlTemplate.Sparql, T]): Future[T] = oneQuery.execute(operation, query)

  implicit class PimpedSparqlTemplate(query: SparqlTemplate.Sparql) {
    private def _get[O](implicit oneQuery: OneQuery[SparqlTemplate.Sparql, O]): Future[O] = execute(QueryOperation.Read, query)

    def get[O](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, _]): Future[O] = _get(queryBuilder())

    def getTx[O, T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, O, T], transactionOps: TransactionOps[T]): Future[O] = _get(transactionOps.query)

    private def _insert(implicit oneQuery: OneQuery[SparqlTemplate.Sparql, Boolean]): Future[Boolean] = execute(QueryOperation.Insert, query)

    def insert(implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, _]): Future[Boolean] = _insert(queryBuilder())

    def insertTx[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, T], transactionOps: TransactionOps[T]): Future[Boolean] = _insert(transactionOps.query)

    private def _update(implicit oneQuery: OneQuery[SparqlTemplate.Sparql, Boolean]): Future[Boolean] = execute(QueryOperation.Update, query)

    def update(implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, _]): Future[Boolean] = _update(queryBuilder())

    def updateTx[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, T], transactionOps: TransactionOps[T]): Future[Boolean] = _update(transactionOps.query)

    private def _delete(implicit oneQuery: OneQuery[SparqlTemplate.Sparql, Boolean]): Future[Boolean] = execute(QueryOperation.Delete, query)

    def delete(implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, _]): Future[Boolean] = _delete(queryBuilder())

    def deleteTx[T <: Transaction](implicit queryBuilder: QueryBuilder[SparqlTemplate.Sparql, Boolean, T], transactionOps: TransactionOps[T]): Future[Boolean] = _delete(transactionOps.query)
  }

}
