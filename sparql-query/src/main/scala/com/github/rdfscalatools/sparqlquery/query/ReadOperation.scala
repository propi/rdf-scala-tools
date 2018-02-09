package com.github.rdfscalatools.sparqlquery.query

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
trait ReadOperation

object ReadOperation {

  case object Ask extends ReadOperation

  case object Construct extends ReadOperation

  case object Describe extends ReadOperation

  case object Select extends ReadOperation

}