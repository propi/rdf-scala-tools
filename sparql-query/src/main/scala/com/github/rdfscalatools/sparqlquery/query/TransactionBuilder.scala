package com.github.rdfscalatools.sparqlquery.query

/**
  * Created by Vaclav Zeman on 15. 8. 2017.
  */
trait TransactionBuilder[T <: Transaction] {

  def apply(): T

}