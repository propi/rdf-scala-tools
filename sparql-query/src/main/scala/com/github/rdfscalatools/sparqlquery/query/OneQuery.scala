package com.github.rdfscalatools.sparqlquery.query

import scala.concurrent.Future

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
trait OneQuery[I, O] {

  def execute(operation: QueryOperation, input: I): Future[O]

}

object OneQuery {

  def apply[I, O, T <: Transaction]()(implicit queryBuilder: QueryBuilder[I, O, T]): OneQuery[I, O] = queryBuilder()

}
