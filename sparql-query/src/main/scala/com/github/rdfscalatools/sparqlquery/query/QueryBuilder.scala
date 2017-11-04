package com.github.rdfscalatools.sparqlquery.query

/**
  * Created by Vaclav Zeman on 15. 8. 2017.
  */
trait QueryBuilder[I, O, T <: Transaction] {

  def apply(): OneQuery[I, O]

  def apply(transaction: T): OneQuery[I, O]

}
