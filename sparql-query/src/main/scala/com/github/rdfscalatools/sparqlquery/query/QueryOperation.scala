package com.github.rdfscalatools.sparqlquery.query

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
sealed trait QueryOperation

object QueryOperation {

  case object Ask extends QueryOperation

  case object Construct extends QueryOperation

  case object Describe extends QueryOperation

  case object Select extends QueryOperation

  case object Update extends QueryOperation

}