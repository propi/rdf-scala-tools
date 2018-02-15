package com.github.rdfscalatools.sparqlquery.query

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by Vaclav Zeman on 15. 8. 2017.
  */
trait QueryBuilder[I, O, T <: Transaction] {

  def apply(transaction: T): OneQuery[I, O]

}

object QueryBuilder {

  implicit def optQueryBuilder[I, O, T <: Transaction](implicit qb: QueryBuilder[I, O, T], ec: ExecutionContext): QueryBuilder[I, Option[O], T] = new QueryBuilder[I, Option[O], T] {
    def mapOneQuery(oq: OneQuery[I, O]): OneQuery[I, Option[O]] = (operation: QueryOperation, input: I) => oq.execute(operation, input).transform {
      case Success(x) => Success(Some(x))
      case Failure(_) => Success(None)
    }

    def apply(transaction: T): OneQuery[I, Option[O]] = mapOneQuery(qb.apply(transaction))
  }

}
