package com.github.rdfscalatools.sparqlquery.query

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
sealed trait QueryOperation

object QueryOperation {

  object Read extends QueryOperation

  object Insert extends QueryOperation

  object Update extends QueryOperation

  object Delete extends QueryOperation

}