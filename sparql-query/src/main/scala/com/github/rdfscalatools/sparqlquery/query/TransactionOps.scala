package com.github.rdfscalatools.sparqlquery.query

/**
  * Created by Vaclav Zeman on 15. 8. 2017.
  */
final class TransactionOps[T <: Transaction](transaction: T) {

  def query[I, O](implicit queryBuilder: QueryBuilder[I, O, T]): OneQuery[I, O] = queryBuilder(transaction)

}
