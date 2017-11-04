package com.github.rdfscalatools.sparqlquery.result

import akka.http.scaladsl.model
import com.github.rdfscalatools.common.CommonExceptions.DeserializationException

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
sealed trait SparqlResult

object SparqlResult {

  type ResultTable = IndexedSeq[Map[String, SparqlResult]]

  case class Uri(uri: String) extends SparqlResult

  sealed trait Literal[T] extends SparqlResult {
    val value: T
  }

  case class StringLiteral(value: String) extends Literal[String]

  case class IntLiteral(value: Int) extends Literal[Int]

  case class DoubleLiteral(value: Double) extends Literal[Double]

  case class BooleanLiteral(value: Boolean) extends Literal[Boolean]

  implicit val valueToJavaUri: SparqlResult => java.net.URI = {
    case SparqlResult.Uri(value) => java.net.URI.create(value)
    case x => throw DeserializationException(s"Value '$x' is not an URI.")
  }

  implicit val valueToAkkaUri: SparqlResult => model.Uri = {
    case SparqlResult.Uri(value) => model.Uri(value)
    case x => throw DeserializationException(s"Value '$x' is not an URI.")
  }

  implicit val valueToString: SparqlResult => String = {
    case SparqlResult.StringLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not a string.")
  }

  implicit val valueToBoolean: SparqlResult => Boolean = {
    case SparqlResult.BooleanLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not boolean.")
  }

  implicit val valueToDouble: SparqlResult => Double = {
    case SparqlResult.DoubleLiteral(value) => value
    case SparqlResult.IntLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not a number.")
  }

  implicit val valueToInt: SparqlResult => Int = {
    case SparqlResult.IntLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not integer.")
  }

}