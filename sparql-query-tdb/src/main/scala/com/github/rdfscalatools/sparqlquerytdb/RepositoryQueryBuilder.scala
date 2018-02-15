package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query._
import org.apache.jena.rdf.model.Model

import scala.concurrent.ExecutionContext

/**
  * Created by Vaclav Zeman on 12. 2. 2018.
  */
object RepositoryQueryBuilder {

  type TxQB[T] = QueryBuilder[SparqlTemplate.Sparql, T, RepositoryTransaction]

  implicit val modelQueryBuilder: TxQB[Model] = (transaction: RepositoryTransaction) => new RepositoryOneQuery.ModelOneQuery(transaction.session)

  implicit val booleanQueryBuilder: TxQB[Boolean] = (transaction: RepositoryTransaction) => new RepositoryOneQuery.BooleanOneQuery(transaction.session)

  implicit val unitQueryBuilder: TxQB[Unit] = (transaction: RepositoryTransaction) => new RepositoryOneQuery.UnitOneQuery(transaction.session)

  implicit val resultTableQueryBuilder: TxQB[ResultTable] = (transaction: RepositoryTransaction) => new RepositoryOneQuery.ResultTableOneQuery(transaction.session)

  implicit def anyFromResultTableQueryBuilder[O](implicit queryBuilder: TxQB[ResultTable], ec: ExecutionContext, convert: ResultTable => O): TxQB[O] = (transaction: RepositoryTransaction) => {
    val oneQuery = queryBuilder(transaction)
    (operation: QueryOperation, input: SparqlTemplate.Sparql) => oneQuery.execute(operation, input).map(convert)
  }

  implicit def anyQueryBuilderAutoCommit[O](implicit queryBuilder: TxQB[O], ec: ExecutionContext, tn: TransactionBuilder[RepositoryTransaction]): QueryBuilder[SparqlTemplate.Sparql, O, Transaction.Empty.type] = (_: Transaction.Empty.type) => new RepositoryOneQuery.AutoCommitOneQuery[O](queryBuilder.apply)

}